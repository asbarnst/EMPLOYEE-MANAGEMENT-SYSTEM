const express = require("express");
const cors = require("cors");
const multer = require("multer");
const XLSX = require("xlsx");

const app = express();
app.use(cors({ origin: (o, cb) => cb(null, true), credentials: true }));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

const SALARY_RANGES = {
  Engineering:[85000,150000],Operations:[55000,90000],Sales:[50000,85000],
  People:[45000,75000],HR:[45000,75000],Design:[65000,100000],
  Analytics:[70000,110000],Finance:[75000,120000],Marketing:[55000,85000],General:[40000,65000],
};
const generateEmpId = (n) => `EMP-${String(n).padStart(4,'0')}`;
const assignSalary = (dept,id) => { const r=SALARY_RANGES[dept]||[40000,80000]; return Math.round((r[0]+((id*7919)%(r[1]-r[0])))/1000)*1000; };

const buildSalaryRecords = (emps) => {
  const now=new Date(),cm=now.getMonth()+1,cy=now.getFullYear(),cd=now.getDate(),recs=[];
  for(let off=3;off>=0;off--){
    let m=cm-off,y=cy; if(m<=0){m+=12;y--;}
    const paid=off>0||(cd>=3); const paidOn=paid?`${y}-${String(m).padStart(2,'0')}-03`:null;
    emps.forEach(e=>recs.push({empId:e.empId,month:m,year:y,amount:e.salary,status:paid?'Paid':'Pending',paidOn}));
  }
  return recs;
};

const INITIAL_EMPLOYEES = [
  {id:1,name:'Alicia Gomez',role:'Operations Lead',department:'Operations',status:'Active',email:'alicia.gomez@company.com',username:'alicia.gomez',joiningDate:'2022-03-15',password:'company123',avatar:null,avatarPreset:1},
  {id:2,name:'Noah Chen',role:'Software Engineer',department:'Engineering',status:'Remote',email:'noah.chen@company.com',username:'noah.chen',joiningDate:'2021-07-01',password:'company123',avatar:null,avatarPreset:2},
  {id:3,name:'Liam Patel',role:'HR Specialist',department:'People',status:'On Leave',email:'liam.patel@company.com',username:'liam.patel',joiningDate:'2023-01-10',password:'company123',avatar:null,avatarPreset:3},
  {id:4,name:'Sofia Rivers',role:'Sales Manager',department:'Sales',status:'Active',email:'sofia.rivers@company.com',username:'sofia.rivers',joiningDate:'2020-11-20',password:'company123',avatar:null,avatarPreset:4},
  {id:5,name:'Marcus Johnson',role:'UI Designer',department:'Design',status:'Active',email:'marcus.johnson@company.com',username:'marcus.johnson',joiningDate:'2022-08-05',password:'company123',avatar:null,avatarPreset:1},
  {id:6,name:'Priya Sharma',role:'Data Analyst',department:'Analytics',status:'Remote',email:'priya.sharma@company.com',username:'priya.sharma',joiningDate:'2023-06-12',password:'company123',avatar:null,avatarPreset:2},
  {id:7,name:'Bala',role:'Software Engineer',department:'Engineering',status:'Active',email:'bala@company.com',username:'bala',joiningDate:'2024-01-10',password:'bala123',avatar:null,avatarPreset:3},
  {id:8,name:'Asbar',role:'UI Designer',department:'Design',status:'Active',email:'asbar@company.com',username:'asbar',joiningDate:'2024-02-15',password:'asbar123',avatar:null,avatarPreset:4},
  {id:9,name:'Nithis',role:'Data Analyst',department:'Analytics',status:'Active',email:'nithis@company.com',username:'nithis',joiningDate:'2024-03-20',password:'nithis123',avatar:null,avatarPreset:1},
  {id:10,name:'Kamalesh',role:'HR Specialist',department:'People',status:'Active',email:'kamalesh@company.com',username:'kamalesh',joiningDate:'2024-04-05',password:'kamalesh123',avatar:null,avatarPreset:2},
].map(e=>({...e,empId:generateEmpId(e.id),salary:assignSalary(e.department,e.id)}));

function makeInitialDB() {
  const employees = INITIAL_EMPLOYEES.map(e=>({...e}));
  const attendance = {};
  const today = new Date().toISOString().split('T')[0];
  employees.forEach((e,i)=>{ attendance[e.id]={[today]:{status:i%3===2?'Absent':'Present',note:i%3===2?'':'Auto-recorded'}}; });
  return {
    employees, attendance,
    schedule:[{id:1,title:'Weekly Team Sync',when:'Mon 10:00 AM',icon:'sync'},{id:2,title:'Performance Review',when:'Wed 2:00 PM',icon:'chart'},{id:3,title:'Training Session',when:'Fri 11:00 AM',icon:'book'}],
    salaryRecords:buildSalaryRecords(employees),
    admin:{username:'admin',password:'admin123'},
    exportLog:[],leaveRequests:[]
  };
}

// Pure in-memory DB — no filesystem. Safe for Vercel serverless.
let DB = makeInitialDB();
const readDB = () => DB;
const writeDB = (d) => { DB = d; };

// Multer memory storage — no disk writes
const upload = multer({ storage: multer.memoryStorage(), limits:{fileSize:5*1024*1024} });

// Health check
app.get('/api/health',(req,res)=>res.json({status:'ok',env:process.env.VERCEL?'vercel':'local'}));

// Login
app.post('/api/login',(req,res)=>{
  const {username,password,role}=req.body;
  if(!username||!password||!role) return res.status(400).json({success:false,message:'Missing fields.'});
  const db=readDB(); const u=username.trim().toLowerCase();
  if(role==='admin'){
    const a=db.admin;
    return u===a.username&&password===a.password
      ?res.json({success:true,role:'admin',user:{name:'Admin User',username:a.username}})
      :res.status(401).json({success:false,message:'Invalid Admin credentials.'});
  }
  const emp=db.employees.find(e=>e.username.toLowerCase()===u&&e.password===password);
  return emp?res.json({success:true,role:'employee',user:emp}):res.status(401).json({success:false,message:'Invalid Employee credentials.'});
});

// Admin password
app.put('/api/admin/password',(req,res)=>{
  const {currentPassword,newPassword}=req.body;
  if(!currentPassword||!newPassword||newPassword.length<6) return res.status(400).json({success:false,message:'Invalid request.'});
  const db=readDB();
  if(db.admin.password!==currentPassword) return res.status(401).json({success:false,message:'Current password incorrect.'});
  db.admin.password=newPassword; writeDB(db);
  res.json({success:true,message:'Password updated.'});
});

// Employee password
app.put('/api/employees/:empId/password',(req,res)=>{
  const db=readDB(); const emp=db.employees.find(e=>e.empId===req.params.empId);
  if(!emp) return res.status(404).json({success:false,message:'Not found.'});
  const {currentPassword,newPassword}=req.body;
  if(emp.password!==currentPassword) return res.status(401).json({success:false,message:'Wrong password.'});
  if(!newPassword||newPassword.length<6) return res.status(400).json({success:false,message:'Password too short.'});
  emp.password=newPassword; writeDB(db); res.json({success:true});
});

// Admin reset employee password
app.put('/api/admin/employees/:empId/password',(req,res)=>{
  const db=readDB(); const emp=db.employees.find(e=>e.empId===req.params.empId);
  if(!emp) return res.status(404).json({success:false,message:'Not found.'});
  const {newPassword}=req.body;
  if(!newPassword||newPassword.length<6) return res.status(400).json({success:false,message:'Password too short.'});
  emp.password=newPassword; writeDB(db); res.json({success:true,message:`Password for ${emp.name} updated.`});
});

// Employees CRUD
app.get('/api/employees',(req,res)=>res.json(readDB().employees));

app.post('/api/employees',(req,res)=>{
  const {name,role,department,salary}=req.body;
  if(!name||!role||!department) return res.status(400).json({success:false,message:'Name, role, department required.'});
  const db=readDB(); const maxId=Math.max(...db.employees.map(e=>e.id),0)+1;
  const newEmp={
    id:maxId,empId:generateEmpId(maxId),name:name.trim(),role:role.trim(),department:department.trim(),
    status:'Active',email:`${name.toLowerCase().replace(/\s+/g,'.')}@company.com`,
    username:name.toLowerCase().replace(/\s+/g,'.'),
    password:`${name.toLowerCase().replace(/\s+/g,'')}123`,
    joiningDate:new Date().toISOString().split('T')[0],
    salary:parseInt(salary,10)||assignSalary(department.trim(),maxId),
    avatar:null,avatarPreset:(maxId%4)+1
  };
  db.employees.push(newEmp); db.attendance[maxId]={};
  const now=new Date(),cm=now.getMonth()+1,cy=now.getFullYear(),cd=now.getDate();
  for(let off=3;off>=0;off--){
    let m=cm-off,y=cy; if(m<=0){m+=12;y--;}
    const paid=off>0||(cd>=3); const paidOn=paid?`${y}-${String(m).padStart(2,'0')}-03`:null;
    db.salaryRecords.push({empId:newEmp.empId,month:m,year:y,amount:newEmp.salary,status:paid?'Paid':'Pending',paidOn});
  }
  writeDB(db); res.status(201).json({success:true,employee:newEmp});
});

app.put('/api/employees/:empId/salary',(req,res)=>{
  const db=readDB(); const emp=db.employees.find(e=>e.empId===req.params.empId);
  if(!emp) return res.status(404).json({success:false,message:'Not found.'});
  const s=parseInt(req.body.salary,10);
  if(isNaN(s)||s<=0) return res.status(400).json({success:false,message:'Invalid salary.'});
  emp.salary=s;
  const now=new Date(); const r=db.salaryRecords.find(r=>r.empId===req.params.empId&&r.month===now.getMonth()+1&&r.year===now.getFullYear());
  if(r) r.amount=s; writeDB(db); res.json({success:true,employee:emp});
});

app.put('/api/employees/:empId/avatar',(req,res)=>{
  const db=readDB(); const emp=db.employees.find(e=>e.empId===req.params.empId);
  if(!emp) return res.status(404).json({success:false,message:'Not found.'});
  const {avatarPreset,avatarBase64}=req.body;
  if(avatarPreset!==undefined){emp.avatarPreset=parseInt(avatarPreset,10);emp.avatar=null;}
  if(avatarBase64!==undefined){emp.avatar=avatarBase64;emp.avatarPreset=0;}
  writeDB(db); res.json({success:true,employee:emp});
});

app.delete('/api/employees/:empId',(req,res)=>{
  const db=readDB(); const idx=db.employees.findIndex(e=>e.empId===req.params.empId);
  if(idx===-1) return res.status(404).json({success:false,message:'Not found.'});
  const emp=db.employees[idx]; db.employees.splice(idx,1);
  delete db.attendance[emp.id]; delete db.attendance[String(emp.id)];
  db.salaryRecords=db.salaryRecords.filter(r=>r.empId!==req.params.empId);
  writeDB(db); res.json({success:true,message:'Employee deleted.'});
});

app.patch('/api/employees/:empId/status',(req,res)=>{
  const db=readDB(); const emp=db.employees.find(e=>e.empId===req.params.empId);
  if(!emp) return res.status(404).json({success:false,message:'Not found.'});
  const valid=['Active','Remote','On Leave'];
  if(!valid.includes(req.body.status)) return res.status(400).json({success:false,message:'Invalid status.'});
  emp.status=req.body.status; writeDB(db); res.json({success:true,employee:emp});
});

// Attendance
app.get('/api/attendance',(req,res)=>res.json(readDB().attendance));
app.post('/api/attendance',(req,res)=>{
  const {empId,date,status,note}=req.body;
  if(!empId||!date||!status) return res.status(400).json({success:false,message:'Missing fields.'});
  const valid=['Present','Absent','Half Day'];
  if(!valid.includes(status)) return res.status(400).json({success:false,message:'Invalid status.'});
  const db=readDB(); if(!db.attendance[empId]) db.attendance[empId]={};
  db.attendance[empId][date]={status,note:(note||'').trim()}; writeDB(db);
  res.json({success:true,attendance:db.attendance[empId][date]});
});

// Schedule
app.get('/api/schedule',(req,res)=>res.json(readDB().schedule));
app.post('/api/schedule',(req,res)=>{
  const {title,when}=req.body;
  if(!title||!when) return res.status(400).json({success:false,message:'title and when required.'});
  const db=readDB(); const id=Math.max(...db.schedule.map(s=>s.id),0)+1;
  const ev={id,title:title.trim(),when:when.trim(),icon:'pin'};
  db.schedule.push(ev); writeDB(db); res.status(201).json({success:true,event:ev});
});

// Salary
app.get('/api/salary',(req,res)=>res.json(readDB().salaryRecords));
app.post('/api/salary/pay',(req,res)=>{
  const {empId,month,year}=req.body;
  if(!empId||!month||!year) return res.status(400).json({success:false,message:'empId, month, year required.'});
  const db=readDB(); const r=db.salaryRecords.find(r=>r.empId===empId&&r.month===month&&r.year===year);
  if(!r) return res.status(404).json({success:false,message:'Record not found.'});
  const now=new Date(); r.status='Paid'; r.paidOn=`${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')}`;
  writeDB(db); res.json({success:true,record:r});
});

// Export Excel
app.get('/api/export/excel',(req,res)=>{
  const db=readDB(); const wb=XLSX.utils.book_new();
  const empData=db.employees.map(e=>({'EMP ID':e.empId,'Name':e.name,'Role':e.role,'Department':e.department,'Status':e.status,'Email':e.email,'Username':e.username,'Joining Date':e.joiningDate,'Monthly Salary (INR)':e.salary}));
  XLSX.utils.book_append_sheet(wb,XLSX.utils.json_to_sheet(empData.length?empData:[{}]),'Employees');
  const attRows=[];
  db.employees.forEach(emp=>{ const a=db.attendance[emp.id]||db.attendance[String(emp.id)]||{}; Object.entries(a).forEach(([date,rec])=>attRows.push({'EMP ID':emp.empId,'Name':emp.name,'Department':emp.department,'Date':date,'Status':rec.status,'Note':rec.note||''})); });
  XLSX.utils.book_append_sheet(wb,XLSX.utils.json_to_sheet(attRows.length?attRows:[{Note:'No records'}]),'Attendance');
  const salData=db.salaryRecords.map(r=>{ const emp=db.employees.find(e=>e.empId===r.empId); return {'EMP ID':r.empId,'Name':emp?emp.name:'','Month':r.month,'Year':r.year,'Amount (INR)':r.amount,'Status':r.status,'Paid On':r.paidOn||''}; });
  XLSX.utils.book_append_sheet(wb,XLSX.utils.json_to_sheet(salData.length?salData:[{Note:'No records'}]),'Salary Records');
  if(!db.exportLog) db.exportLog=[];
  db.exportLog.push({timestamp:new Date().toISOString(),type:'excel',exportedBy:'admin',recordCount:db.employees.length});
  writeDB(db);
  const buf=XLSX.write(wb,{type:'buffer',bookType:'xlsx'});
  res.setHeader('Content-Disposition','attachment; filename="Ashes-Tech-Employee-Data.xlsx"');
  res.setHeader('Content-Type','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
  res.send(buf);
});
app.get('/api/export/log',(req,res)=>res.json(readDB().exportLog||[]));

// Leave
app.get('/api/leave',(req,res)=>res.json(readDB().leaveRequests||[]));
app.post('/api/leave',(req,res)=>{
  const {empId,empName,type,from,to,reason}=req.body;
  if(!empId||!empName||!type||!from||!to||!reason) return res.status(400).json({success:false,message:'All fields required.'});
  const db=readDB(); if(!db.leaveRequests) db.leaveRequests=[];
  const newReq={id:Date.now(),empId,empName,type,from,to,reason:reason.trim(),status:'Pending',appliedOn:new Date().toISOString().split('T')[0]};
  db.leaveRequests.unshift(newReq); writeDB(db); res.status(201).json({success:true,leaveRequest:newReq});
});
app.patch('/api/leave/:id/status',(req,res)=>{
  const db=readDB(); if(!db.leaveRequests) db.leaveRequests=[];
  const r=db.leaveRequests.find(r=>r.id===parseInt(req.params.id,10));
  if(!r) return res.status(404).json({success:false,message:'Not found.'});
  const valid=['Approved','Rejected','Pending'];
  if(!valid.includes(req.body.status)) return res.status(400).json({success:false,message:'Invalid status.'});
  r.status=req.body.status; r.updatedOn=new Date().toISOString().split('T')[0];
  writeDB(db); res.json({success:true,leaveRequest:r});
});
app.delete('/api/leave/:id',(req,res)=>{
  const db=readDB(); if(!db.leaveRequests) db.leaveRequests=[];
  const idx=db.leaveRequests.findIndex(r=>r.id===parseInt(req.params.id,10));
  if(idx===-1) return res.status(404).json({success:false,message:'Not found.'});
  db.leaveRequests.splice(idx,1); writeDB(db); res.json({success:true,message:'Deleted.'});
});

// Meet signaling
const rooms={};
app.post('/api/meet/join',(req,res)=>{
  const {roomId,userId,name}=req.body;
  if(!roomId||!userId) return res.status(400).json({error:'Missing params.'});
  if(!rooms[roomId]) rooms[roomId]={participants:[],signals:[]};
  if(!rooms[roomId].participants.find(p=>p.userId===userId)) rooms[roomId].participants.push({userId,name});
  res.json({success:true,participants:rooms[roomId].participants});
});
app.post('/api/meet/signal',(req,res)=>{
  const {roomId,from,to,type,signal}=req.body;
  if(!roomId||!from||!to) return res.status(400).json({error:'Missing params.'});
  if(!rooms[roomId]) rooms[roomId]={participants:[],signals:[]};
  rooms[roomId].signals.push({from,to,type,signal}); res.json({success:true});
});
app.get('/api/meet/signals/:roomId/:userId',(req,res)=>{
  const {roomId,userId}=req.params; const room=rooms[roomId];
  if(!room) return res.json({signals:[]});
  const sigs=room.signals.filter(s=>s.to===userId);
  room.signals=room.signals.filter(s=>s.to!==userId); res.json({signals:sigs});
});
app.post('/api/meet/leave',(req,res)=>{
  const {roomId,userId}=req.body; const room=rooms[roomId];
  if(room){ room.participants=room.participants.filter(p=>p.userId!==userId); room.signals=room.signals.filter(s=>s.from!==userId&&s.to!==userId); if(!room.participants.length) delete rooms[roomId]; }
  res.json({success:true});
});

if(require.main===module){ const PORT=process.env.PORT||5000; app.listen(PORT,()=>console.log(`Server on port ${PORT}`)); }
module.exports=app;