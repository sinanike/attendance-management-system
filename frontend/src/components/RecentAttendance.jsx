export default function RecentAttendance({rows}){

  function openAllAttendance(){
    window.location.assign('/admin/attendance')
  }

  return <article className="panel dashboard-action-panel">
    <div className="panel-head">
      <div>
        <span className="panel-kicker">ترددهای اخیر</span>
        <h2>آخرین ترددها</h2>
      </div>
    </div>

    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            <th>نام و نام خانوادگی</th>
            <th>واحد سازمانی</th>
            <th>نوع تردد</th>
            <th>ساعت</th>
            <th>وضعیت</th>
          </tr>
        </thead>

        <tbody>
          {rows.map(r=>
            <tr key={r.id}>
              <td>{r.name}</td>
              <td>{r.department}</td>
              <td>
                <span className={`status-pill status-${r.state}`}>
                  {r.type}
                </span>
              </td>
              <td>{r.time}</td>
              <td>
                <span className="status-dot"></span>
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>

    <button
      type="button"
      className="card-link dashboard-footer-action"
      onClick={openAllAttendance}
    >
      مشاهده همه ترددها ←
    </button>
  </article>
}
