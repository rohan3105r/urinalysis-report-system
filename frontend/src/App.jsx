import { useEffect, useMemo, useState } from 'react'

const API = 'http://localhost:8080/api/tests'
const blank = {
  sampleId: '', testDate: new Date().toISOString().slice(0, 10), ph: '6.0',
  specificGravity: '1.020', urineColor: 'Pale yellow', glucose: 'Negative', protein: 'Negative',
  ketones: 'Negative', blood: 'Negative', nitrite: 'Negative',
  leukocytes: 'Negative', notes: ''
}
const fields = [
  ['glucose','Glucose'], ['protein','Protein'], ['ketones','Ketones'],
  ['blood','Blood'], ['nitrite','Nitrite'], ['leukocytes','Leukocytes']
]
const options = ['Negative', 'Trace', '1+', '2+', '3+', 'Positive']

function Icon({ name }) {
  const paths = {
    grid: <><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></>,
    file: <><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6M8 13h8M8 17h8"/></>,
    plus: <><path d="M12 5v14M5 12h14"/></>,
    clock: <><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></>,
    check: <><path d="m5 12 4 4L19 6"/></>,
    alert: <><path d="M10.3 3.9 2.4 18a2 2 0 0 0 1.7 3h15.8a2 2 0 0 0 1.7-3l-7.9-14.1a2 2 0 0 0-3.4 0Z"/><path d="M12 9v4m0 4h.01"/></>,
    search: <><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
    trash: <><path d="M3 6h18M8 6V4h8v2m3 0-1 14H6L5 6m4 4v6m6-6v6"/></>,
    edit: <><path d="m16 4 4 4L9 19l-5 1 1-5Z"/></>,
    close: <><path d="m6 6 12 12M18 6 6 18"/></>
  }
  return <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name] || paths.file}</svg>
}

function App() {
  const [tests, setTests] = useState([])
  const [page, setPage] = useState('dashboard')
  const [form, setForm] = useState(blank)
  const [editingId, setEditingId] = useState(null)
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  async function loadTests() {
    setLoading(true)
    try {
      const response = await fetch(API)
      if (!response.ok) throw new Error('Backend returned an error. Check that Spring Boot and MongoDB are running.')
      setTests(await response.json())
      setError('')
    } catch (e) {
      setError('Cannot connect to the backend. Start MongoDB and run the Spring Boot server on port 8080.')
    } finally { setLoading(false) }
  }
  useEffect(() => { loadTests() }, [])

  const flagged = tests.filter(t => t.reviewStatus === 'Review recommended')
  const latestReport = tests[0] || null
  const filtered = useMemo(() => tests.filter(t =>
    `${t.sampleId} ${t.testDate} ${t.reviewStatus} ${t.patientMessage || ''} ${t.doctorSummary || ''}`.toLowerCase().includes(query.toLowerCase())
  ), [tests, query])

  function startNew() {
    setForm({ ...blank, sampleId: `SAMPLE-${String(Date.now()).slice(-5)}` })
    setEditingId(null); setError(''); setNotice(''); setPage('new')
  }
  function startEdit(test) {
    setForm({ ...blank, ...test }); setEditingId(test.id); setError(''); setNotice(''); setPage('new')
  }
  function updateField(event) {
    setForm(prev => ({ ...prev, [event.target.name]: event.target.value }))
  }
  async function saveTest(event) {
    event.preventDefault(); setError(''); setNotice('')
    try {
      const response = await fetch(editingId ? `${API}/${editingId}` : API, {
        method: editingId ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form)
      })
      if (!response.ok) {
        const body = await response.json().catch(() => ({}))
        throw new Error(Object.values(body).join(', ') || 'Could not save record.')
      }
      await loadTests()
      setNotice(editingId ? 'Test record updated.' : 'Test record saved.')
      setPage('history')
    } catch (e) { setError(e.message || 'Could not save record.') }
  }
  async function deleteTest(id) {
    if (!window.confirm('Delete this test record?')) return
    try {
      const response = await fetch(`${API}/${id}`, { method: 'DELETE' })
      if (!response.ok) throw new Error('Could not delete record.')
      await loadTests(); setNotice('Test record deleted.')
    } catch (e) { setError(e.message) }
  }
  function nav(next) { setPage(next); setError(''); setNotice('') }

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark">U</div><div><strong>UrinaCare</strong><small>LAB MANAGEMENT</small></div></div>
      <div className="nav-label">WORKSPACE</div>
      <button className={`nav-item ${page==='dashboard'?'active':''}`} onClick={() => nav('dashboard')}><Icon name="grid"/> Dashboard</button>
      <button className={`nav-item ${page==='new'?'active':''}`} onClick={startNew}><Icon name="plus"/> New test report</button>
      <button className={`nav-item ${page==='history'?'active':''}`} onClick={() => nav('history')}><Icon name="clock"/> Report history</button>
      <div className="sidebar-bottom"><div className="safe-icon"><Icon name="check"/></div><div><b>Demo environment</b><p>Use sample data only. Not for clinical decisions.</p></div></div>
    </aside>

    <main className="main">
      <header className="topbar"><div><span className="eyebrow">LABORATORY WORKSPACE</span><h1>{page==='dashboard'?'Dashboard':page==='new'?(editingId?'Edit test report':'Create test report'):'Report history'}</h1></div><div className="top-right"><span className="live-dot"/> System workspace <div className="avatar">LA</div></div></header>
      <div className="content">
        {error && <div className="banner error"><Icon name="alert"/><span>{error}</span><button onClick={() => setError('')} aria-label="Dismiss"><Icon name="close"/></button></div>}
        {notice && <div className="banner success"><Icon name="check"/><span>{notice}</span><button onClick={() => setNotice('')} aria-label="Dismiss"><Icon name="close"/></button></div>}

        {page==='dashboard' && <>
          <div className="welcome-row"><div><h2>Good day, Lab Team</h2><p>Here's an overview of your urinalysis records.</p></div><button className="primary" onClick={startNew}><Icon name="plus"/> Create new report</button></div>
          <div className="stat-grid">
            <Stat icon="file" label="Total test records" value={tests.length} note="All saved records" tone="blue"/>
            <Stat icon="alert" label="Flagged for review" value={flagged.length} note="Rule-based demo flags" tone="amber"/>
            <Stat icon="check" label="No demo flags" value={tests.length-flagged.length} note="No rules triggered" tone="green"/>
          </div>
          <div className="section-card">
            <div className="section-head"><div><h3>Recent test reports</h3><p>Latest records saved in the database</p></div><button className="text-button" onClick={() => nav('history')}>View all reports <span>→</span></button></div>
            <TestTable tests={tests.slice(0,5)} onEdit={startEdit} onDelete={deleteTest} loading={loading}/>
          </div>
          <div className="section-card">
            <h3>Patient-facing summary</h3>
            <p>{latestReport ? latestReport.patientMessage || 'No abnormal findings were detected in this urine sample.' : 'No abnormal findings were detected. This urine sample is within the illustrative reference range.'}</p>
            {latestReport && <div className="report-meta"><span className={`priority-pill ${latestReport.priorityLevel || 'Low'}`}>{latestReport.priorityLevel || 'Low'}</span><span>Urine colour: {latestReport.urineColor || 'Pale yellow'}</span></div>}
          </div>
          <div className="note-card"><div className="note-icon"><Icon name="alert"/></div><div><b>About result flagging</b><p>Flags are generated by illustrative software rules to demonstrate decision-support workflow. They are not diagnoses and should not be used to guide real patient care.</p></div></div>
        </>}

        {page==='new' && <div className="section-card form-card">
          <div className="section-head"><div><h3>{editingId?'Update test information':'Enter urinalysis information'}</h3><p>Fields marked * are required. Use sample or anonymized data.</p></div><button className="icon-button" onClick={() => nav('history')} aria-label="Close"><Icon name="close"/></button></div>
          <form onSubmit={saveTest}>
            <div className="form-section-title">Sample details</div>
            <div className="form-grid two">
              <label>Sample ID *<input name="sampleId" value={form.sampleId} onChange={updateField} required maxLength="80" placeholder="e.g. SAMPLE-1001"/></label>
              <label>Test date *<input type="date" name="testDate" value={form.testDate || ''} onChange={updateField} required/></label>
            </div>
            <div className="form-section-title">Physical / chemical observations</div>
            <div className="form-grid two">
              <label>Urine colour<select name="urineColor" value={form.urineColor || 'Pale yellow'} onChange={updateField}><option>Pale yellow</option><option>Clear</option><option>Light yellow</option><option>Dark yellow</option><option>Amber</option><option>Red</option><option>Brown</option><option>Cloudy</option><option>Orange</option></select></label>
              <label>pH <span className="hint">(numeric)</span><input name="ph" type="number" min="0" max="14" step="0.1" value={form.ph ?? ''} onChange={updateField} placeholder="e.g. 6.0"/></label>
              <label>Specific gravity <span className="hint">(numeric)</span><input name="specificGravity" type="number" min="0.9" max="1.1" step="0.001" value={form.specificGravity ?? ''} onChange={updateField} placeholder="e.g. 1.020"/></label>
              {fields.map(([key,label]) => <label key={key}>{label}<select name={key} value={form[key] || 'Negative'} onChange={updateField}>{options.map(o=><option key={o}>{o}</option>)}</select></label>)}
            </div>
            <div className="form-section-title">Additional notes</div>
            <label>Notes <textarea name="notes" value={form.notes || ''} onChange={updateField} rows="3" maxLength="1000" placeholder="Optional notes for this demonstration record"/></label>
            <div className="form-footer"><span className="hint">The system flags selected values for review; it does not diagnose.</span><div><button type="button" className="secondary" onClick={() => nav('history')}>Cancel</button><button className="primary" type="submit"><Icon name="check"/>{editingId?'Save changes':'Save test report'}</button></div></div>
          </form>
        </div>}

        {page==='history' && <div className="section-card">
          <div className="section-head history-head"><div><h3>All test reports</h3><p>Search, review, edit, or remove saved test records.</p></div><button className="primary" onClick={startNew}><Icon name="plus"/> New report</button></div>
          <div className="search-box"><Icon name="search"/><input value={query} onChange={e=>setQuery(e.target.value)} placeholder="Search by sample ID, date, or status..."/></div>
          <TestTable tests={filtered} onEdit={startEdit} onDelete={deleteTest} loading={loading}/>
        </div>}

        {latestReport && page !== 'new' && <div className="section-card report-panel">
          <div className="section-head"><div><h3>Clinical report review</h3><p>Separate normal and abnormal findings with priority levels</p></div><span className={`priority-pill ${latestReport.priorityLevel || 'Low'}`}>{latestReport.priorityLevel || 'Low'}</span></div>
          <div className="report-grid">
            <div className="report-block normal"><h4>Normal findings</h4><ul>{(latestReport.normalFindings || []).map((item, index) => <li key={index}>{item}</li>) || <li>No normal findings recorded.</li>}</ul></div>
            <div className="report-block abnormal"><h4>Abnormal findings</h4><ul>{(latestReport.abnormalFindings || []).map((item, index) => <li key={index}>{item}</li>) || <li>No abnormal findings recorded.</li>}</ul></div>
          </div>
          <div className="report-grid">
            <div className="report-block patient"><h4>Patient version</h4><p>{latestReport.patientMessage}</p></div>
            <div className="report-block doctor"><h4>Doctor version</h4><p>{latestReport.doctorSummary}</p></div>
          </div>
        </div>}
        <footer>UrinaCare · Student full-stack project · Educational demonstration only</footer>
      </div>
    </main>
  </div>
}

function Stat({icon,label,value,note,tone}) {
  return <div className="stat-card"><div className={`stat-icon ${tone}`}><Icon name={icon}/></div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-note">{note}</div></div>
}
function TestTable({tests,onEdit,onDelete,loading}) {
  if (loading) return <div className="empty"><div className="loader"/><b>Loading records...</b></div>
  if (!tests.length) return <div className="empty"><div className="empty-mark"><Icon name="file"/></div><b>No test reports yet</b><p>Create a test report to see it listed here.</p></div>
  return <div className="table-wrap"><table><thead><tr><th>Sample ID</th><th>Test date</th><th>pH</th><th>Glucose</th><th>Protein</th><th>Review status</th><th>Priority</th><th>Patient summary</th><th>Actions</th></tr></thead><tbody>
    {tests.map(t=><tr key={t.id}><td><b className="sample-id">{t.sampleId}</b></td><td>{t.testDate || '—'}</td><td>{t.ph || '—'}</td><td>{t.glucose || '—'}</td><td>{t.protein || '—'}</td><td><span className={`status ${t.reviewStatus==='Review recommended'?'warning':'clear'}`}><span/>{t.reviewStatus || 'Unreviewed'}</span></td><td><span className={`priority-pill ${t.priorityLevel || 'Low'}`}>{t.priorityLevel || 'Low'}</span></td><td style={{maxWidth:'260px',whiteSpace:'normal'}}>{t.patientMessage || 'No abnormal findings were detected in this urine sample.'}</td><td><div className="actions"><button className="icon-button" onClick={()=>onEdit(t)} title="Edit"><Icon name="edit"/></button><button className="icon-button danger-icon" onClick={()=>onDelete(t.id)} title="Delete"><Icon name="trash"/></button></div></td></tr>)}
  </tbody></table></div>
}
export default App
