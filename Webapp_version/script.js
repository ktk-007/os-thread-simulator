// ============================================================
//  COLORS & STATE
// ============================================================
const COLORS = [
  '#7c3aed','#059669','#d97706','#dc2626',
  '#2563eb','#db2777','#0891b2','#65a30d',
  '#7c2d12','#1e3a8a','#4d7c0f','#9333ea'
];

let processes = [];
let pid_counter = 1;
let tid_counter = 1;
let clock = 0;
let running = false;
let simInterval = null;
let kthreads = [];
let semaphores = {};
let activeTab = 0;
let ganttHistory = [];

// Monitor action log for display
let monitorLog = [];

// ============================================================
//  HELPERS
// ============================================================
const threadModel = () => document.getElementById('thread-model').value;
const schedAlgo  = () => document.getElementById('sched-algo').value;
const coreCount  = () => Math.max(1, Math.min(16, parseInt(document.getElementById('core-count').value) || 4));
const timeQ      = () => Math.max(1, parseInt(document.getElementById('time-q').value) || 3);
const syncMode   = () => document.querySelector('input[name=sync]:checked').value;
const configMode = () => document.querySelector('input[name=config]:checked').value;
const burstInput = () => Math.max(1, parseInt(document.getElementById('burst-input').value) || 6);

function getColor(i){ return COLORS[i % COLORS.length]; }
function hex2rgba(hex, a){
  const r=parseInt(hex.slice(1,3),16), g=parseInt(hex.slice(3,5),16), b=parseInt(hex.slice(5,7),16);
  return `rgba(${r},${g},${b},${a})`;
}
function allThreads(){ return processes.flatMap(p => p.threads); }
function getThread(tid){ for(const p of processes) for(const t of p.threads) if(t.tid===tid) return t; return null; }
function procColor(pid){ const p=processes.find(x=>x.pid===pid); return p?p.color:'#aaa'; }

// ============================================================
//  THREAD / PROCESS FACTORIES
// ============================================================
function makeThread(proc){
  const burst = configMode()==='Manual' ? burstInput() : (3 + Math.floor(Math.random()*8));
  return {
    tid: 'T'+tid_counter++,
    pid: proc.pid,
    burstTime: burst,
    remainingTime: burst,
    priority: Math.floor(Math.random()*5)+1,
    status: 'ready',
    core: null,
    kthread: null,
    waitTime: 0,
    turnaround: 0,
    startTime: null,
    finishTime: null,
    syncStatus: '—',
    memory: (64+Math.floor(Math.random()*192))+'MB',
    quantum: 0,
    terminated: false,
    monitorAction: null,
  };
}

// Add process with ONE thread by default (via button), or zero if silent
function addProcess(silent=false){
  const color = getColor(processes.length);
  const proc = { pid:'P'+pid_counter++, color, threads:[] };
  if(!silent){
    // Add exactly ONE thread by default when user clicks Add Process
    proc.threads.push(makeThread(proc));
  }
  processes.push(proc);
  if(!silent){ buildKthreads(); buildSemaphores(); render(); }
}

function addThread(pid){
  const proc = processes.find(p=>p.pid===pid);
  if(proc){ proc.threads.push(makeThread(proc)); buildKthreads(); buildSemaphores(); render(); }
}

function terminateProcess(pid){
  const proc = processes.find(p=>p.pid===pid);
  if(proc){
    proc.threads.forEach(t=>{
      if(t.status!=='done' && t.status!=='terminated'){
        t.status='terminated';
        t.finishTime=clock;
        t.core=null;
        t.terminated=true;
        // release any semaphore held
        Object.values(semaphores).forEach(s=>{ if(s.owner===t.tid){ s.value++; s.owner=null; }});
      }
    });
  }
  render();
}

// ============================================================
//  KERNEL THREAD MAPPING
// ============================================================
function buildKthreads(){
  const model = threadModel();
  const active = processes.flatMap(p => p.threads.filter(t=>t.status!=='done'&&t.status!=='terminated'));
  kthreads = [];
  let ki = 1;
  if(model==='one-one'){
    active.forEach(t => kthreads.push({id:'KT'+ki++, threads:[t.tid], pid:t.pid}));
  } else if(model==='many-one'){
    processes.forEach(p => {
      const at = p.threads.filter(t=>t.status!=='done'&&t.status!=='terminated');
      if(at.length>0) kthreads.push({id:'KT'+ki++, threads:at.map(t=>t.tid), pid:p.pid});
    });
  } else { // many-many
    for(let i=0;i<active.length;i+=2){
      kthreads.push({id:'KT'+ki++, threads:active.slice(i,i+2).map(t=>t.tid), pid:active[i].pid});
    }
  }
  kthreads.forEach(k => k.threads.forEach(tid => { const t=getThread(tid); if(t) t.kthread=k.id; }));
}

// ============================================================
//  SEMAPHORES
// ============================================================
function buildSemaphores(){
  semaphores = {};
  const at = allThreads().filter(t=>t.status!=='done'&&t.status!=='terminated');
  const n = Math.min(3, Math.max(1, Math.floor(at.length/2)));
  for(let i=0;i<n;i++){
    semaphores['K'+(i+1)] = {value:1, owner:null, waitList:[]};
  }
}

// Monitor action descriptions
const MONITOR_ACTIONS = [
  'Acquiring lock','Executing CS','Releasing lock','Waiting on cond','Signaling cond',
  'Entering monitor','Notifying waiters','Condition met','Re-acquiring lock','Exiting monitor'
];

function pickMonitorAction(){
  return MONITOR_ACTIONS[Math.floor(Math.random()*MONITOR_ACTIONS.length)];
}

// ============================================================
//  SIMULATION STEP
// ============================================================
function toggleStart(){
  running = !running;
  const btn = document.getElementById('start-btn');
  if(running){
    btn.textContent='⏸ Pause'; btn.classList.add('running');
    simInterval = setInterval(doStep, 750); // slightly slower animation
  } else {
    btn.textContent='▶ Start'; btn.classList.remove('running');
    clearInterval(simInterval);
  }
}

function doStep(){
  clock++;
  const algo = schedAlgo();
  const cores = coreCount();
  const all = allThreads();
  const moves = [];

  // --- Tick running threads ---
  all.filter(t=>t.status==='running').forEach(t => {
    t.remainingTime = Math.max(0, t.remainingTime - 1);
    t.turnaround++;
    t.quantum++;

    // Update monitor action if in monitor mode
    if(syncMode()==='Monitors'){
      if(Math.random()<0.4) t.monitorAction = pickMonitorAction();
    } else {
      t.monitorAction = null;
    }

    if(t.remainingTime <= 0){
      t.status='done'; t.finishTime=clock; t.monitorAction=null;
      Object.values(semaphores).forEach(s=>{ if(s.owner===t.tid){ s.value++; s.owner=null; }});
      moves.push({tid:t.tid, pid:t.pid, from:'Core '+t.core, to:'✓ Done', color:procColor(t.pid)});
      ganttHistory.push({color:procColor(t.pid), label:t.pid+'·'+t.tid});
      t.core=null;
    } else if(algo==='RR' && t.quantum >= timeQ()){
      t.status='ready'; t.quantum=0;
      moves.push({tid:t.tid, pid:t.pid, from:'Core '+t.core, to:'Ready Q', color:procColor(t.pid)});
      ganttHistory.push({color:procColor(t.pid), label:t.pid+'·'+t.tid});
      t.core=null;
    }
  });

  // --- Tick waiting threads (semaphore/monitor release chance) ---
  all.filter(t=>t.status==='waiting').forEach(t => {
    t.waitTime++; t.turnaround++;
    if(Math.random() < 0.30){
      t.status='ready'; t.syncStatus='—'; t.monitorAction=null;
      moves.push({tid:t.tid, pid:t.pid, from:'Waiting', to:'Ready Q', color:procColor(t.pid)});
    }
  });

  // --- Random non-sync waiting: rare chance for any ready thread to stall briefly ---
  all.filter(t=>t.status==='ready').forEach(t => {
    // ~3% chance per tick to spontaneously wait (rare, not sync-related)
    if(Math.random() < 0.03){
      t.status='waiting';
      t.syncStatus='I/O Wait';
      moves.push({tid:t.tid, pid:t.pid, from:'Ready', to:'⏸ Waiting(I/O)', color:procColor(t.pid)});
    }
  });

  // --- Tick ready threads (accumulate wait) ---
  all.filter(t=>t.status==='ready').forEach(t => t.waitTime++);

  // --- Schedule: sort ready queue ---
  let queue = all.filter(t=>t.status==='ready');
  if(algo==='SJF') queue.sort((a,b)=>a.remainingTime-b.remainingTime);
  else if(algo==='Priority') queue.sort((a,b)=>b.priority-a.priority);

  // --- Assign to free cores ---
  const usedCores = new Set(all.filter(t=>t.status==='running').map(t=>t.core));
  for(let c=1;c<=cores;c++){
    if(!usedCores.has(c) && queue.length>0){
      const t = queue.shift();
      // Semaphore contention (15% chance, only if semaphores exist & mode is Semaphores)
      const semKeys = Object.keys(semaphores);
      if(semKeys.length>0 && syncMode()==='Semaphores' && Math.random()<0.15){
        const sem = semaphores[semKeys[Math.floor(Math.random()*semKeys.length)]];
        if(sem.value<=0){
          t.status='waiting'; t.syncStatus='Blocked';
          moves.push({tid:t.tid, pid:t.pid, from:'Ready', to:'⏸ Waiting(Sem)', color:procColor(t.pid)});
          continue;
        } else {
          sem.value--; sem.owner=t.tid;
          t.syncStatus='Acquired';
          setTimeout(()=>{
            const sk = semKeys[0];
            if(semaphores[sk]){ semaphores[sk].value++; semaphores[sk].owner=null; }
          }, 2500+Math.random()*3500);
        }
      }
      // Monitor contention (10% chance if Monitors mode)
      if(syncMode()==='Monitors' && Math.random()<0.10){
        t.status='waiting'; t.syncStatus='Mon.Wait';
        t.monitorAction='Waiting on cond';
        moves.push({tid:t.tid, pid:t.pid, from:'Ready', to:'⏸ Waiting(Mon)', color:procColor(t.pid)});
        continue;
      }
      t.status='running'; t.core=c; t.startTime=t.startTime||clock; t.quantum=0;
      if(syncMode()==='Monitors') t.monitorAction = pickMonitorAction();
      usedCores.add(c);
      moves.push({tid:t.tid, pid:t.pid, from:'Ready', to:'Core '+c, color:procColor(t.pid)});
    }
  }

  buildKthreads();
  showMoves(moves);
  if(ganttHistory.length > 80) ganttHistory = ganttHistory.slice(-60);
  render();

  // Auto stop
  const nonTerminated = all.filter(t=>t.status!=='terminated');
  if(nonTerminated.length>0 && nonTerminated.every(t=>t.status==='done')){
    if(running) toggleStart();
  }
}

function resetSim(){
  if(running) toggleStart();
  processes=[]; pid_counter=1; tid_counter=1; clock=0; kthreads=[]; semaphores={}; ganttHistory=[]; monitorLog=[];
  document.getElementById('move-items').innerHTML='';
  init();
}

// ============================================================
//  MOVE NOTIFICATIONS
// ============================================================
function showMoves(moves){
  const el = document.getElementById('move-items');
  moves.forEach(m => {
    const d = document.createElement('div');
    d.className='move-arrow';
    d.style.borderColor=m.color+'55';
    d.style.background=hex2rgba(m.color,0.1);
    d.innerHTML=`<span style="color:${m.color};font-weight:700">${m.pid}·${m.tid}</span><span style="color:var(--text2);margin:0 4px">→</span><span style="color:var(--text)">${m.to}</span>`;
    el.appendChild(d);
    setTimeout(()=>{ d.style.animation='fadeout .5s forwards'; setTimeout(()=>d.remove(),500); }, 3500);
  });
}

// ============================================================
//  TAB SWITCH
// ============================================================
function switchTab(i){
  activeTab=i;
  document.getElementById('detail-wrap').style.display = i===0?'':'none';
  document.getElementById('timing-wrap').style.display = i===1?'':'none';
  document.getElementById('tab0-btn').classList.toggle('active',i===0);
  document.getElementById('tab1-btn').classList.toggle('active',i===1);
}

// ============================================================
//  RENDER
// ============================================================
function render(){
  renderProcessList();
  renderCores();
  renderTables();
  renderKthreads();
  renderSemaphores();
  renderGantt();
  renderStatusBar();
}

function renderProcessList(){
  const el = document.getElementById('process-list');
  el.innerHTML='';
  processes.forEach(proc => {
    const alive = proc.threads.filter(t=>t.status!=='done'&&t.status!=='terminated').length;
    const div = document.createElement('div');
    div.className='process-row';
    div.style.borderColor=proc.color+'55';
    div.innerHTML=`
      <div class="process-header" style="background:${hex2rgba(proc.color,0.1)}">
        <div class="proc-dot" style="background:${proc.color}"></div>
        <span class="proc-name" style="color:${proc.color}">${proc.pid}</span>
        <span class="proc-tcount">${alive}/${proc.threads.length}t</span>
      </div>
      <div class="thread-list">
        ${proc.threads.map(t=>`
          <span class="thread-pill" title="${t.tid}: ${t.status}, rem=${t.remainingTime}"
            style="background:${hex2rgba(proc.color,0.15)};color:${proc.color};border-color:${proc.color}44;
                   opacity:${(t.status==='done'||t.status==='terminated')?0.4:1}">
            ${t.tid}<span style="color:var(--text2);margin-left:3px;font-size:10px">${t.status.slice(0,3)}</span>
          </span>`).join('')}
      </div>
      <div class="proc-actions">
        <button class="btn" onclick="addThread('${proc.pid}')">+Thread</button>
        <button class="btn danger" onclick="terminateProcess('${proc.pid}')">✕ Kill</button>
      </div>
    `;
    el.appendChild(div);
  });
}

function renderCores(){
  const grid = document.getElementById('cores-grid');
  const n = coreCount();
  const cols = n<=4?n : n<=8?4 : 4;
  grid.style.gridTemplateColumns=`repeat(${cols},1fr)`;
  if(grid.children.length !== n){
    grid.innerHTML='';
    for(let c=1;c<=n;c++){
      const div=document.createElement('div');
      div.id='core-'+c; div.className='core-box';
      div.innerHTML=`<div class="core-label">Core ${c}</div><div id="core-body-${c}"><div class="core-empty">idle</div></div><div class="core-bar" id="cbar-${c}" style="width:0%"></div>`;
      grid.appendChild(div);
    }
  }
  for(let c=1;c<=n;c++){
    const box=document.getElementById('core-'+c);
    const body=document.getElementById('core-body-'+c);
    const bar=document.getElementById('cbar-'+c);
    const threads=allThreads().filter(t=>t.status==='running'&&t.core===c);
    if(threads.length===0){
      box.classList.remove('active'); box.classList.add('idle-pulse');
      body.innerHTML='<div class="core-empty">idle</div>';
      bar.style.width='0%'; bar.style.background='var(--border2)';
    } else {
      box.classList.add('active'); box.classList.remove('idle-pulse');
      const proc=processes.find(p=>p.pid===threads[0].pid);
      const col=proc?proc.color:'#aaa';
      body.innerHTML=threads.map(t=>`
        <div class="core-thread" style="background:${hex2rgba(col,0.15)};border-color:${col}33">
          <span class="core-thread-name" style="color:${col}">${t.pid}·${t.tid}</span>
          <span style="color:var(--text2);font-size:10px;margin-left:5px">pri:${t.priority}</span>
          ${t.monitorAction?`<span style="color:var(--yellow);font-size:10px;margin-left:4px">[${t.monitorAction}]</span>`:''}
          <span class="core-thread-rem">${t.remainingTime}/${t.burstTime}t</span>
        </div>`).join('');
      const pct=Math.round((1-threads[0].remainingTime/threads[0].burstTime)*100);
      bar.style.width=pct+'%'; bar.style.background=col;
    }
  }
}

function renderTables(){
  const all=allThreads();
  // Detail table
  const tbody=document.getElementById('thread-tbody');
  tbody.innerHTML='';
  all.forEach(t=>{
    const col=procColor(t.pid);
    const tr=document.createElement('tr');
    tr.innerHTML=`
      <td style="color:${col};font-weight:700">${t.pid}</td>
      <td>${t.tid}</td>
      <td style="color:var(--text2)">${schedAlgo()}</td>
      <td>${t.burstTime}</td>
      <td>${timeQ()}</td>
      <td>${t.priority}</td>
      <td style="color:${t.remainingTime===0?'var(--red)':t.status==='running'?'var(--green)':'var(--text)'};font-weight:600">${t.remainingTime}</td>
      <td style="color:${t.syncStatus!=='—'?'var(--yellow)':'var(--text2)'}">${t.syncStatus}</td>
      <td style="color:var(--text2)">${t.memory}</td>
    `;
    tbody.appendChild(tr);
  });
  // Timing table
  const timingTbody=document.getElementById('timing-tbody');
  timingTbody.innerHTML='';
  all.forEach(t=>{
    const col=procColor(t.pid);
    const displayStatus = t.terminated ? 'terminated' : t.status;
    const tr=document.createElement('tr');
    tr.innerHTML=`
      <td style="color:${col};font-weight:700">${t.pid}</td>
      <td>${t.tid}</td>
      <td><span class="status-badge s-${displayStatus}">${displayStatus}</span></td>
      <td>${t.waitTime}</td>
      <td>${(t.status==='done'||t.terminated)?(t.finishTime-(t.startTime||0)):t.turnaround}</td>
    `;
    timingTbody.appendChild(tr);
  });
  // Status table
  const statusTbody=document.getElementById('status-tbody');
  statusTbody.innerHTML='';
  all.forEach(t=>{
    const col=procColor(t.pid);
    const displayStatus = t.terminated ? 'terminated' : t.status;
    const tr=document.createElement('tr');
    tr.innerHTML=`
      <td style="color:${col};font-weight:700">${t.pid}</td>
      <td>${t.tid}</td>
      <td>${t.core?'Core '+t.core:'—'}</td>
      <td style="color:var(--text2)">${t.kthread||'—'}</td>
      <td><span class="status-badge s-${displayStatus}">${displayStatus}</span></td>
    `;
    statusTbody.appendChild(tr);
  });
}

function renderKthreads(){
  const el=document.getElementById('kthread-wrap');
  el.innerHTML='';
  if(kthreads.length===0){
    el.innerHTML='<div style="padding:8px;color:var(--border2);font-size:11px">No kernel threads</div>';
    return;
  }
  kthreads.forEach(k=>{
    const proc=processes.find(p=>p.pid===k.pid);
    const col=proc?proc.color:'#aaa';
    const row=document.createElement('div');
    row.className='kthread-row';
    row.innerHTML=`
      <span class="kthread-id">${k.id}</span>
      <span class="kthread-arrow">→</span>
      ${k.threads.map(tid=>`
        <span class="ut-badge" style="background:${hex2rgba(col,0.18)};color:${col};border-color:${col}44">${tid}</span>
      `).join('')}
    `;
    el.appendChild(row);
  });
}

function renderSemaphores(){
  const el=document.getElementById('sync-viz');
  el.innerHTML='';
  if(syncMode()==='Monitors'){
    // Show monitor status + what each running thread is doing
    const runningThreads = allThreads().filter(t=>t.status==='running'||t.status==='waiting');
    let html = '<div class="sem-box"><span class="sem-name">Monitor</span><span class="sem-val free">Active</span><span class="sem-waiting">Mutual exclusion via condition variables</span></div>';
    if(runningThreads.length>0){
      runningThreads.forEach(t=>{
        const col = procColor(t.pid);
        const action = t.monitorAction || (t.status==='waiting'?'Waiting on cond':'In critical section');
        html += `<span class="monitor-thread-badge" style="background:${hex2rgba(col,0.15)};border-color:${col}55;color:${col}">
          ${t.pid}·${t.tid} <span class="m-action">→ ${action}</span>
        </span>`;
      });
    }
    el.innerHTML=html;
    return;
  }
  const keys=Object.keys(semaphores);
  if(keys.length===0){
    el.innerHTML='<span style="color:var(--text2);font-size:11px">No semaphores</span>';
    return;
  }
  keys.forEach(k=>{
    const s=semaphores[k];
    const div=document.createElement('div');
    div.className='sem-box';
    div.innerHTML=`
      <span class="sem-name">${k}</span>
      <span class="sem-val ${s.value>0?'free':'locked'}">${s.value}</span>
      <span class="sem-waiting">${s.owner?`held: ${s.owner}`:'free'}</span>
    `;
    el.appendChild(div);
  });
}

function renderGantt(){
  const track=document.getElementById('gantt-track');
  const W=track.offsetWidth||600;
  const slice=Math.max(20, Math.floor(W/ganttHistory.length)||35);
  track.innerHTML=ganttHistory.map(g=>`
    <div class="gantt-block" style="width:${Math.min(slice,60)}px;background:${hex2rgba(g.color,0.7)};color:${g.color};min-width:35px" title="${g.label}">${slice>16?g.label:''}</div>
  `).join('');
  track.scrollLeft=track.scrollWidth;
}

function renderStatusBar(){
  const all=allThreads();
  document.getElementById('clock').textContent=clock;
  document.getElementById('stat-procs').textContent=processes.length;
  document.getElementById('stat-threads').textContent=all.length;
  document.getElementById('stat-running').textContent=all.filter(t=>t.status==='running').length;
  document.getElementById('stat-waiting').textContent=all.filter(t=>t.status==='waiting').length;
  document.getElementById('stat-done').textContent=all.filter(t=>t.status==='done'||t.terminated).length;
  document.getElementById('mode-disp').textContent=threadModel().toUpperCase()+' | '+schedAlgo();
  const dot=document.getElementById('tick-dot');
  dot.classList.add('pulse'); setTimeout(()=>dot.classList.remove('pulse'),150);
}

// ============================================================
//  EVENT LISTENERS
// ============================================================
document.getElementById('thread-model').addEventListener('change',()=>{buildKthreads();render();});
document.getElementById('sched-algo').addEventListener('change',function(){
  document.getElementById('tq-group').style.display=this.value==='RR'?'flex':'none';
  const tqGroup = document.getElementById('tq-group');
  //const tqInput = document.getElementById('time-q');

  if(this.value === 'RR'){
    tqGroup.style.display = 'flex';
    document.getElementById('time-q').focus();
  } else {
    tqGroup.style.display = 'none';
  }

  render();
});
document.getElementById('core-count').addEventListener('change',()=>{
  document.getElementById('cores-grid').innerHTML='';
  render();
});
document.querySelectorAll('input[name=sync]').forEach(r=>r.addEventListener('change',()=>{buildSemaphores();render();}));
document.querySelectorAll('input[name=config]').forEach(r=>r.addEventListener('change',function(){
  document.getElementById('bt-group').style.display=this.value==='Manual'?'flex':'none';
}));

// ============================================================
//  INIT — No default processes, user adds via button
// ============================================================
function init(){
  const tqGroup = document.getElementById('tq-group');
  tqGroup.style.display = (schedAlgo() === 'RR') ? 'flex' : 'none';
  // No seeded processes — start empty, user clicks Add Process
  buildKthreads();
  buildSemaphores();
  render();
}

init();
