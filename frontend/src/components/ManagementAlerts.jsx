import {AlertTriangle} from 'lucide-react'

export default function ManagementAlerts({alerts}){

  function openAllAlerts(){
    window.location.assign('/admin/reports/daily-attendance')
  }

  return <article className="panel dashboard-action-panel">

    <div className="panel-head">
      <div>
        <span className="panel-kicker">هشدارها</span>
        <h2>هشدارهای سیستمی</h2>
      </div>

      <AlertTriangle
        size={18}
        color="#ff4b55"
      />
    </div>

    <div className="alert-list">
      {alerts.map(a=>
        <div
          className={`alert-row ${a.tone}`}
          key={a.id}
        >
          <span className="alert-dot"></span>
          <span>{a.text}</span>
        </div>
      )}
    </div>

    <button
      type="button"
      className="card-link dashboard-footer-action"
      onClick={openAllAlerts}
    >
      مشاهده همه هشدارها ←
    </button>
  </article>
}
