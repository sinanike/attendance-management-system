export default function PendingRequests({rows}){

  function openAllRequests(){
    window.location.assign('/admin/leaves')
  }

  return <article className="panel dashboard-action-panel">
    <div className="panel-head">
      <div>
        <span className="panel-kicker">درخواست‌ها</span>
        <h2>درخواست‌های در انتظار</h2>
      </div>
    </div>

    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            <th>نوع درخواست</th>
            <th>نام</th>
            <th>واحد</th>
            <th>تاریخ</th>
            <th>وضعیت</th>
          </tr>
        </thead>

        <tbody>
          {rows.map(r=>
            <tr key={r.id}>
              <td>{r.type}</td>
              <td>{r.name}</td>
              <td>{r.department}</td>
              <td>{r.date}</td>
              <td>
                <span className="status-pill status-wait">
                  {r.status}
                </span>
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>

    <button
      type="button"
      className="card-link dashboard-footer-action"
      onClick={openAllRequests}
    >
      مشاهده همه درخواست‌ها ←
    </button>
  </article>
}
