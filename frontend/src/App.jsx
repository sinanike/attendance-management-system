import { useEffect, useMemo, useState } from 'react'
import {
  BarChart3,
  Bell,
  BriefcaseBusiness,
  CalendarCheck2,
  CalendarDays,
  ChevronDown,
  CircleHelp,
  Clock3,
  FileBarChart2,
  Fingerprint,
  Fullscreen,
  Home,
  KeyRound,
  ListChecks,
  LogOut,
  Menu,
  Minimize2,
  Plane,
  ShieldAlert,
  UserRound,
  UsersRound,
  X,
} from 'lucide-react'
import './App.css'
import emblem from './assets/sut-emblem-hq.png'

import StatCard from './components/StatCard'
import DepartmentChart from './components/DepartmentChart'
import AttendanceTrendChart from './components/AttendanceTrendChart'
import AttendanceStatusChart from './components/AttendanceStatusChart'
import RecentAttendance from './components/RecentAttendance'
import PendingRequests from './components/PendingRequests'
import SmartAnalysis from './components/SmartAnalysis'
import ManagementAlerts from './components/ManagementAlerts'

const navGroups = [
  {
    title: null,
    items: [
      {
        label: 'داشبورد',
        icon: Home,
        href: '/admin/dashboard',
        active: true,
      },
    ],
  },
  {
    title: 'مدیریت',
    items: [
      {
        label: 'کارکنان',
        icon: UsersRound,
        href: '/admin/employees',
      },
      {
        label: 'شیفت‌های کاری',
        icon: Clock3,
        href: '/admin/shifts',
      },
      {
        label: 'تخصیص شیفت روزانه',
        icon: CalendarCheck2,
        href: '/admin/shift-assignments',
      },
      {
        label: 'ترددهای ثبت‌شده',
        icon: Fingerprint,
        href: '/admin/attendance',
      },
      {
        label: 'درخواست‌های مرخصی',
        icon: ListChecks,
        href: '/admin/leaves',
      },
      {
        label: 'درخواست‌های مأموریت',
        icon: BriefcaseBusiness,
        href: '/admin/missions',
      },
      {
        label: 'تعطیلات رسمی',
        icon: CalendarDays,
        href: '/admin/holidays',
      },
    ],
  },
  {
    title: 'گزارش‌ها',
    items: [
      {
        label: 'گزارش ورود و خروج',
        icon: FileBarChart2,
        href: '/admin/reports/attendance',
      },
      {
        label: 'گزارش روزانه و شیفتی',
        icon: BarChart3,
        href: '/admin/reports/daily-attendance',
      },
      {
        label: 'خلاصه کارکرد کارکنان',
        icon: ListChecks,
        href: '/admin/reports/attendance-summary',
      },
    ],
  },
  {
    title: 'تنظیمات',
    items: [
      {
        label: 'کاربران و سطح دسترسی',
        icon: KeyRound,
        href: '/admin/users',
      },
    ],
  },
]


function faDate(date){
  return new Intl.DateTimeFormat('fa-IR-u-ca-persian',{
    year:'numeric',month:'2-digit',day:'2-digit'
  }).format(date)
}


function toPersianDigits(value){
  return String(value ?? '')
    .replace(
      /\d/g,
      digit =>
        '۰۱۲۳۴۵۶۷۸۹'[Number(digit)],
    )
}


function formatPersianDate(value){
  if (!value) {
    return '—'
  }

  return new Intl.DateTimeFormat(
    'fa-IR-u-ca-persian',
    {
      year:'numeric',
      month:'2-digit',
      day:'2-digit',
    },
  ).format(
    new Date(
      `${value}T00:00:00`,
    ),
  )
}


function formatPersianShortDate(value){
  if (!value) {
    return ''
  }

  return new Intl.DateTimeFormat(
    'fa-IR-u-ca-persian',
    {
      month:'short',
      day:'numeric',
    },
  ).format(
    new Date(
      `${value}T00:00:00`,
    ),
  )
}

export default function App(){

  const RETURN_LOADING_CLASS =
    'dashboard-return-loading'

  const BOOT_LOADING_CLASS =
    'dashboard-boot-loading'


  const showReturnLoading =
    () => {

      document.documentElement.classList.add(
        RETURN_LOADING_CLASS,
      )
    }


  const hideReturnLoading =
    () => {

      document.documentElement.classList.remove(
        RETURN_LOADING_CLASS,
      )

      document.documentElement.classList.remove(
        BOOT_LOADING_CLASS,
      )
    }


  const [dashboardData, setDashboardData] =
    useState(null)

  const [dashboardError, setDashboardError] =
    useState(false)

  const [aiData, setAiData] =
    useState(null)

  const [aiLoading, setAiLoading] =
    useState(true)

  const [now,setNow]=
    useState(new Date())

  const [mobileOpen,setMobileOpen]=
    useState(false)

  const [logoutLoading,setLogoutLoading]=
    useState(false)

  const [notificationOpen,setNotificationOpen]=
    useState(false)

  const [helpOpen,setHelpOpen]=
    useState(false)

  const [profileOpen,setProfileOpen]=
    useState(false)

  const [profileData,setProfileData]=
    useState(null)

  const [profileLoading,setProfileLoading]=
    useState(true)

  const [profileError,setProfileError]=
    useState(false)

  const [isFullscreen,setIsFullscreen]=
    useState(Boolean(document.fullscreenElement))


  useEffect(()=>{

    const timer=
      setInterval(
        ()=>setNow(new Date()),
        1000,
      )

    return()=>clearInterval(timer)

  },[])


  useEffect(()=>{

    const handleFullscreenChange =
      () => {

        setIsFullscreen(
          Boolean(
            document.fullscreenElement,
          ),
        )
      }


    document.addEventListener(
      'fullscreenchange',
      handleFullscreenChange,
    )


    return () => {

      document.removeEventListener(
        'fullscreenchange',
        handleFullscreenChange,
      )
    }

  },[])


  useEffect(()=>{

    let cancelled =
      false


    async function loadProfile(){

      try {

        setProfileLoading(true)

        setProfileError(false)


        const response =
          await fetch(
            '/admin/api/security/profile',
            {
              method:'GET',
              credentials:'include',
              headers:{
                Accept:'application/json',
              },
            },
          )


        if (!response.ok) {

          throw new Error(
            'Profile API failed.',
          )
        }


        const result =
          await response.json()


        if (!cancelled) {

          setProfileData(result)
        }


      } catch (error) {

        console.error(
          'Admin profile failed:',
          error,
        )


        if (!cancelled) {

          setProfileError(true)
        }


      } finally {

        if (!cancelled) {

          setProfileLoading(false)
        }
      }
    }


    loadProfile()


    return () => {

      cancelled =
        true
    }

  },[])


  useEffect(() => {

    let cancelled =
      false


    async function loadDashboard() {

      try {

        const response =
          await fetch(
            '/admin/api/dashboard/data',
            {
              method: 'GET',
              credentials: 'include',
              headers: {
                Accept: 'application/json',
              },
            },
          )


        const contentType =
          response.headers.get(
            'content-type',
          ) ?? ''


        if (
          !response.ok
          ||
          !contentType.includes(
            'application/json',
          )
        ) {

          throw new Error(
            'Dashboard API is not available.',
          )
        }


        const result =
          await response.json()


        if (!cancelled) {

          setDashboardData(result)

          setDashboardError(false)

          /*
           * داده جدید رسید. در فریم بعدی ماسک بازگشت
           * برداشته می‌شود تا حتی یک فریم از آمار قبلی
           * نمایش داده نشود.
           */
          window.requestAnimationFrame(
            hideReturnLoading,
          )
        }


      } catch (error) {

        if (!cancelled) {

          console.error(
            'Admin live dashboard failed:',
            error,
          )

          setDashboardError(true)

          hideReturnLoading()
        }
      }
    }


    loadDashboard()


    /*
     * اگر کاربر از صفحه دیگری به داشبورد برگردد،
     * اطلاعات قدیمی Browser Back/Forward Cache
     * حتی برای چند ثانیه نمایش داده نشود.
     */
    /*
     * قبل از اینکه مرورگر صفحه را داخل Back/Forward Cache
     * ذخیره کند، یک ماسک Loading روی DOM می‌گذاریم.
     * بنابراین Snapshot ذخیره‌شده دیگر آمار قبلی را ندارد.
     */
    const handlePageHide =
      () => {

        showReturnLoading()
      }


    const handlePageShow =
      event => {

        if (event.persisted) {

          /*
           * صفحه از BFCache برگشته است.
           * ماسک Loading باقی می‌ماند تا داده تازه برسد.
           */
          setDashboardData(null)

          loadDashboard()

          return
        }


        /*
         * ورود معمولی، نه بازگشت از BFCache.
         */
        hideReturnLoading()
      }


    /*
     * وقتی تب دوباره فعال می‌شود،
     * همان لحظه آخرین اطلاعات را می‌گیریم.
     */
    const handleVisibilityChange =
      () => {

        if (
          document.visibilityState
          === 'visible'
        ) {

          loadDashboard()
        }
      }


    const handleWindowFocus =
      () => {

        loadDashboard()
      }


    window.addEventListener(
      'pagehide',
      handlePageHide,
    )

    window.addEventListener(
      'pageshow',
      handlePageShow,
    )

    document.addEventListener(
      'visibilitychange',
      handleVisibilityChange,
    )

    window.addEventListener(
      'focus',
      handleWindowFocus,
    )


    const refreshTimer =
      window.setInterval(
        loadDashboard,
        10000,
      )


    return () => {

      cancelled =
        true

      window.clearInterval(
        refreshTimer,
      )

      window.removeEventListener(
        'pagehide',
        handlePageHide,
      )

      window.removeEventListener(
        'pageshow',
        handlePageShow,
      )

      hideReturnLoading()

      document.removeEventListener(
        'visibilitychange',
        handleVisibilityChange,
      )

      window.removeEventListener(
        'focus',
        handleWindowFocus,
      )
    }

  }, [])


  useEffect(() => {

    let cancelled =
      false


    async function loadAi() {

      try {

        setAiLoading(true)


        const response =
          await fetch(
            '/admin/api/dashboard/ai',
            {
              method: 'GET',
              credentials: 'include',
              headers: {
                Accept: 'application/json',
              },
            },
          )


        if (!response.ok) {

          throw new Error(
            'AI dashboard API failed.',
          )
        }


        const result =
          await response.json()


        if (!cancelled) {

          setAiData(result)
        }


      } catch (error) {

        console.error(
          'Dashboard Gemini analysis failed:',
          error,
        )


        if (!cancelled) {

          setAiData({
            available: false,
            analysis:
              'تحلیل هوشمند در حال حاضر در دسترس نیست.',
          })
        }


      } finally {

        if (!cancelled) {

          setAiLoading(false)
        }
      }
    }


    loadAi()


    /*
     * Gemini هر 5 دقیقه Refresh می‌شود،
     * نه هر 10 ثانیه.
     */

    const aiRefreshTimer =
      window.setInterval(
        loadAi,
        300000,
      )


    return () => {

      cancelled =
        true

      window.clearInterval(
        aiRefreshTimer,
      )
    }

  }, [])


  const overview =
    dashboardData?.overview


  const liveStats =
    overview

      ? [
          {
            title:'کارکنان فعال',
            value:overview.activeEmployeeCount,
            unit:'نفر',
            tone:'cyan',
            icon:UsersRound,
          },
          {
            title:'امروز حاضر',
            value:overview.presentCount,
            unit:'نفر',
            tone:'green',
            icon:UserRound,
          },
          {
            title:'امروز غایب',
            value:overview.absentCount,
            unit:'نفر',
            tone:'red',
            icon:ShieldAlert,
          },
          {
            title:'مرخصی',
            value:overview.leaveCount,
            unit:'نفر',
            tone:'purple',
            icon:BriefcaseBusiness,
          },
          {
            title:'ماموریت',
            value:overview.missionCount,
            unit:'نفر',
            tone:'blue',
            icon:Plane,
          },
          {
            title:'تاخیر',
            value:overview.lateCount,
            unit:'نفر',
            tone:'orange',
            icon:Clock3,
          },
          {
            title:'ورود ناقص',
            value:overview.incompleteCount,
            unit:'نفر',
            tone:'amber',
            icon:Fingerprint,
          },
          {
            title:'نرخ حضور',
            value:
              `${Number(
                overview.attendanceRate,
              ).toLocaleString(
                'fa-IR',
                {
                  maximumFractionDigits:1,
                },
              )}٪`,
            unit:'درصد',
            tone:'teal',
            icon:Clock3,
          },
        ]

      : [
          {
            title:'کارکنان فعال',
            value:'—',
            unit:'در حال دریافت',
            tone:'cyan',
            icon:UsersRound,
          },
          {
            title:'امروز حاضر',
            value:'—',
            unit:'در حال دریافت',
            tone:'green',
            icon:UserRound,
          },
          {
            title:'امروز غایب',
            value:'—',
            unit:'در حال دریافت',
            tone:'red',
            icon:ShieldAlert,
          },
          {
            title:'مرخصی',
            value:'—',
            unit:'در حال دریافت',
            tone:'purple',
            icon:BriefcaseBusiness,
          },
          {
            title:'ماموریت',
            value:'—',
            unit:'در حال دریافت',
            tone:'blue',
            icon:Plane,
          },
          {
            title:'تاخیر',
            value:'—',
            unit:'در حال دریافت',
            tone:'orange',
            icon:Clock3,
          },
          {
            title:'ورود ناقص',
            value:'—',
            unit:'در حال دریافت',
            tone:'amber',
            icon:Fingerprint,
          },
          {
            title:'نرخ حضور',
            value:'—',
            unit:'در حال دریافت',
            tone:'teal',
            icon:Clock3,
          },
        ]


  const liveTrend =
    dashboardData?.trend?.length

      ? dashboardData.trend.map(
          item => ({
            day:
              formatPersianShortDate(
                item.date,
              ),
            rate:
              item.rate,
          }),
        )

      : []


  const statusColors = {
    'حاضر':'#21a774',
    'غایب':'#ff4d57',
    'مرخصی':'#a855f7',
    'مأموریت':'#2563eb',
    'ورود ناقص':'#f59e0b',
  }


  const liveStatus =
    dashboardData?.status?.length

      ? dashboardData.status.map(
          item => ({
            name:
              item.name,
            value:
              item.value,
            color:
              statusColors[item.name]
              ?? '#64748b',
          }),
        )

      : []


  const liveDepartments =
    dashboardData?.departments?.length

      ? dashboardData.departments.map(
          item => ({
            name:
              item.name,
            rate:
              item.rate,
          }),
        )

      : []


  const liveRecent =
    dashboardData?.recentAttendance

      ? dashboardData.recentAttendance.map(
          item => ({
            id:
              item.id,
            name:
              item.name,
            department:
              item.department,
            type:
              item.type,
            time:
              toPersianDigits(
                item.time,
              ),
            state:
              item.state,
          }),
        )

      : []


  const liveRequests =
    dashboardData?.pendingRequests

      ? dashboardData.pendingRequests.map(
          item => ({
            id:
              item.key,
            type:
              item.type,
            name:
              item.name,
            department:
              item.department,
            date:
              formatPersianDate(
                item.date,
              ),
            status:
              item.status,
          }),
        )

      : []


  const liveAlerts =
    dashboardData?.alerts?.length

      ? dashboardData.alerts

      : [
          {
            id:'loading-live-data',
            tone:'blue',
            text:'در حال دریافت آخرین اطلاعات سامانه...',
          },
        ]


  const date=
    useMemo(
      ()=>faDate(now),
      [now],
    )


  const dashboardUpdatedAt =
    dashboardData?.generatedAt
      ?? dashboardData?.overview?.generatedAt
      ?? null


  const lastUpdateTime =
    dashboardUpdatedAt

      ? new Intl.DateTimeFormat(
          'fa-IR',
          {
            hour:'2-digit',
            minute:'2-digit',
            second:'2-digit',
          },
        ).format(
          new Date(
            dashboardUpdatedAt,
          ),
        )

      : '—'


  const alertNotificationMeta = {

    absence:{
      title:'غیبت امروز',
      href:'/admin/reports/daily-attendance?status=ABSENT',
    },

    late:{
      title:'تأخیر امروز',
      href:'/admin/reports/daily-attendance',
    },

    incomplete:{
      title:'تردد ناقص',
      href:'/admin/reports/daily-attendance?status=INCOMPLETE',
    },
  }


  const requestNotificationItems =
    liveRequests.map(
      request => ({

        id:
          `request-${request.id}`,

        title:
          request.type,

        text:
          `${request.name} — ${request.department}`,

        href:
          request.type
            ?.includes('مأموریت')
              ? '/admin/missions'
              : '/admin/leaves',

        tone:
          'request',
      }),
    )


  const alertNotificationItems =
    liveAlerts
      .filter(
        alert =>
          Boolean(
            alertNotificationMeta[
              alert.id
            ],
          ),
      )
      .map(
        alert => {

          const meta =
            alertNotificationMeta[
              alert.id
            ]


          return {

            id:
              `alert-${alert.id}`,

            title:
              meta.title,

            text:
              alert.text,

            href:
              meta.href,

            tone:
              alert.tone
              ?? 'alert',
          }
        },
      )


  const notificationItems = [

    ...requestNotificationItems,

    ...alertNotificationItems,
  ]


  const notificationCount =
    notificationItems.length


  async function toggleFullscreen(){

    try {

      if (!document.fullscreenElement) {

        await document.documentElement
          .requestFullscreen()

      } else {

        await document.exitFullscreen()
      }


    } catch (error) {

      console.error(
        'Fullscreen failed:',
        error,
      )
    }
  }


  function toggleNotifications(){

    setNotificationOpen(
      current => !current,
    )

    setHelpOpen(false)

    setProfileOpen(false)
  }


  function toggleHelp(){

    setHelpOpen(
      current => !current,
    )

    setNotificationOpen(false)

    setProfileOpen(false)
  }


  function toggleProfile(){

    setProfileOpen(
      current => !current,
    )

    setNotificationOpen(false)

    setHelpOpen(false)
  }


  async function handleLogout(){

    if (logoutLoading) {
      return
    }

    try {

      setLogoutLoading(true)


      const csrfResponse =
        await fetch(
          '/admin/api/security/csrf',
          {
            method:'GET',
            credentials:'include',
            headers:{
              Accept:'application/json',
            },
          },
        )


      if (!csrfResponse.ok) {

        throw new Error(
          'CSRF token request failed.',
        )
      }


      const csrf =
        await csrfResponse.json()


      const logoutResponse =
        await fetch(
          '/logout',
          {
            method:'POST',
            credentials:'include',
            redirect:'follow',
            headers:{
              [csrf.headerName]:
                csrf.token,
            },
          },
        )


      if (!logoutResponse.ok) {

        throw new Error(
          'Logout request failed.',
        )
      }


      window.location.replace(
        '/login?logout',
      )


    } catch (error) {

      console.error(
        'Logout failed:',
        error,
      )

      /*
       * Fallback رسمی Spring Security:
       * GET /logout صفحه تایید Logout را نشان می‌دهد.
       */
      window.location.assign(
        '/logout',
      )
    }
  }


  return <div className="dashboard-shell">
    {mobileOpen && <button className="mobile-backdrop" onClick={()=>setMobileOpen(false)} aria-label="بستن منو" />}
    <button className="mobile-menu" onClick={()=>setMobileOpen(true)} aria-label="باز کردن منو"><Menu size={21}/></button>

    <aside className={`sidebar ${mobileOpen?'sidebar-open':''}`}>
      <button className="sidebar-close" onClick={()=>setMobileOpen(false)}><X size={18}/></button>

      <div className="brand-block">
        <img
          src={emblem}
          alt="نشان دانشگاه صنعتی شیراز"
          className="brand-logo"
        />

        <div className="brand-university-fa">
          دانشگاه صنعتی شیراز
        </div>

        <div className="brand-university-en">
          SHIRAZ UNIVERSITY
          <br />
          OF TECHNOLOGY
        </div>

        <div className="brand-system-title">
          سامانه مدیریت حضور و غیاب
        </div>
      </div>

      <nav className="nav-list">
        {navGroups.map((group, groupIndex) => (
          <div
            className="nav-group"
            key={group.title ?? `root-${groupIndex}`}
          >
            {group.title && (
              <div className="nav-group-title">
                {group.title}
              </div>
            )}

            {group.items.map(
              ({
                label,
                icon: Icon,
                href,
                active,
              }) => (
                <a
                  key={label}
                  href={href}
                  className={`nav-item ${
                    active
                      ? 'active'
                      : ''
                  }`}
                >
                  <Icon size={19} />
                  <span>{label}</span>
                </a>
              ),
            )}
          </div>
        ))}
      </nav>

      <button
        type="button"
        className="sidebar-logout"
        onClick={handleLogout}
        disabled={logoutLoading}
      >
        <LogOut size={18}/>
        <span>
          {
            logoutLoading
              ? 'در حال خروج...'
              : 'خروج از سامانه'
          }
        </span>
      </button>
    </aside>

    <main className="main-content">
      <header className="top-header">
        <div className="headline">
          <h1>داشبورد مدیر امور اداری</h1>

          <div className="live-line">

            <span
              className={`system-status ${
                dashboardData
                  ? 'online'
                  : dashboardError
                    ? 'offline'
                    : 'connecting'
              }`}
            >
              <span className="system-status-dot"></span>

              {
                dashboardData
                  ? 'سامانه آنلاین'
                  : dashboardError
                    ? 'اختلال در ارتباط'
                    : 'در حال اتصال'
              }
            </span>

            {
              dashboardData
              &&
              <span className="last-update">
                آخرین بروزرسانی:
                {' '}
                {lastUpdateTime}
              </span>
            }

          </div>
        </div>

        <div className="header-left">

          <div className="today">
            <CalendarDays size={18}/>
            <span>
              امروز:
              {' '}
              {date}
            </span>
          </div>


          <div className="header-action-wrap">

            <button
              type="button"
              className={`header-icon ${
                isFullscreen
                  ? 'active'
                  : ''
              }`}
              onClick={toggleFullscreen}
              aria-label={
                isFullscreen
                  ? 'خروج از حالت تمام صفحه'
                  : 'نمایش تمام صفحه'
              }
              title={
                isFullscreen
                  ? 'خروج از تمام صفحه'
                  : 'نمایش تمام صفحه'
              }
            >
              {
                isFullscreen
                  ? <Minimize2 size={19}/>
                  : <Fullscreen size={19}/>
              }
            </button>

          </div>


          <div className="header-action-wrap">

            <button
              type="button"
              className={`header-icon ${
                helpOpen
                  ? 'active'
                  : ''
              }`}
              onClick={toggleHelp}
              aria-expanded={helpOpen}
              aria-label="راهنمای داشبورد"
              title="راهنمای داشبورد"
            >
              <CircleHelp size={19}/>
            </button>


            {
              helpOpen
              &&
              <div className="header-popover help-popover">

                <div className="popover-head">

                  <div>
                    <strong>
                      راهنمای داشبورد
                    </strong>

                    <span>
                      دسترسی سریع مدیر امور اداری
                    </span>
                  </div>

                  <button
                    type="button"
                    className="popover-close"
                    onClick={
                      () => setHelpOpen(false)
                    }
                    aria-label="بستن"
                  >
                    <X size={16}/>
                  </button>

                </div>


                <div className="help-list">

                  <div className="help-item">
                    <b>
                      بروزرسانی زنده
                    </b>

                    <span>
                      اطلاعات داشبورد به‌صورت خودکار تازه می‌شود.
                    </span>
                  </div>


                  <div className="help-item">
                    <b>
                      اعلان‌ها
                    </b>

                    <span>
                      درخواست‌ها و هشدارهای نیازمند بررسی را از زنگ ببینید.
                    </span>
                  </div>


                  <div className="help-item">
                    <b>
                      گزارش‌ها
                    </b>

                    <span>
                      گزارش‌های حضور، روزانه و خلاصه کارکرد از منوی سمت راست در دسترس‌اند.
                    </span>
                  </div>

                </div>

              </div>
            }

          </div>


          <div className="header-action-wrap">

            <button
              type="button"
              className={`header-icon notification ${
                notificationOpen
                  ? 'active'
                  : ''
              }`}
              onClick={toggleNotifications}
              aria-expanded={notificationOpen}
              aria-label="اعلان‌های مدیریتی"
              title="اعلان‌های مدیریتی"
            >
              <Bell size={19}/>

              {
                notificationCount > 0
                &&
                <b>
                  {
                    toPersianDigits(
                      Math.min(
                        notificationCount,
                        99,
                      ),
                    )
                  }
                </b>
              }
            </button>


            {
              notificationOpen
              &&
              <div className="header-popover notification-popover">

                <div className="popover-head">

                  <div>
                    <strong>
                      اعلان‌های مدیریتی
                    </strong>

                    <span>
                      {
                        notificationCount > 0
                          ? `${toPersianDigits(notificationCount)} مورد نیازمند توجه`
                          : 'مورد جدیدی وجود ندارد'
                      }
                    </span>
                  </div>

                  <button
                    type="button"
                    className="popover-close"
                    onClick={
                      () => setNotificationOpen(false)
                    }
                    aria-label="بستن"
                  >
                    <X size={16}/>
                  </button>

                </div>


                <div className="notification-list">

                  {
                    notificationItems.length === 0

                      ? <div className="notification-empty">
                          در حال حاضر اعلان جدیدی وجود ندارد.
                        </div>

                      : notificationItems
                          .slice(0, 6)
                          .map(
                            item => {

                              const content =
                                <>
                                  <span
                                    className={`notification-item-dot ${
                                      item.tone
                                    }`}
                                  ></span>

                                  <span className="notification-item-content">
                                    <b>
                                      {item.title}
                                    </b>

                                    <small>
                                      {item.text}
                                    </small>
                                  </span>
                                </>


                              return <a
                                key={item.id}
                                className="notification-item"
                                href={item.href}
                                title={`رفتن به ${item.title}`}
                              >
                                {content}

                                <span
                                  className="notification-go"
                                  aria-hidden="true"
                                >
                                  ←
                                </span>
                              </a>
                            },
                          )
                  }

                </div>


                {
                  notificationCount > 0
                  &&
                  <div className="popover-footer">

                    <a href="/admin/leaves">
                      مرخصی‌ها
                    </a>

                    <a href="/admin/missions">
                      مأموریت‌ها
                    </a>

                  </div>
                }

              </div>
            }

          </div>


          <div className="user-profile-wrap">

            <button
              type="button"
              className={`user-chip ${
                profileOpen
                  ? 'active'
                  : ''
              }`}
              onClick={toggleProfile}
              aria-expanded={profileOpen}
              aria-label="مشاهده مشخصات حساب"
            >
              <span className="avatar">
                <UserRound size={22}/>
              </span>

              <span>
                مدیر امور اداری
              </span>

              <ChevronDown
                size={15}
                className={
                  profileOpen
                    ? 'user-chevron-open'
                    : ''
                }
              />
            </button>


            {
              profileOpen
              &&
              <div className="header-popover profile-popover">

                <div className="profile-summary">

                  <div className="profile-avatar-large">
                    <UserRound size={30}/>
                  </div>

                  <div className="profile-summary-text">

                    <strong>
                      {
                        profileData?.fullName
                        ||
                        'مدیر امور اداری'
                      }
                    </strong>

                    <span>
                      {
                        profileData?.roleDisplayName
                        ||
                        'مدیر امور اداری'
                      }
                    </span>

                  </div>


                  <button
                    type="button"
                    className="popover-close"
                    onClick={
                      () => setProfileOpen(false)
                    }
                    aria-label="بستن"
                  >
                    <X size={16}/>
                  </button>

                </div>


                {
                  profileLoading

                    ? <div className="profile-loading">
                        در حال دریافت مشخصات حساب...
                      </div>

                    : profileError

                      ? <div className="profile-error">
                          دریافت مشخصات حساب ممکن نشد.
                        </div>

                      : <div className="profile-details">

                          <div className="profile-detail-row">
                            <span>
                              نام کاربری
                            </span>

                            <b dir="ltr">
                              {profileData?.username ?? '—'}
                            </b>
                          </div>


                          <div className="profile-detail-row">
                            <span>
                              سطح دسترسی
                            </span>

                            <b>
                              {profileData?.roleDisplayName ?? '—'}
                            </b>
                          </div>


                          <div className="profile-detail-row">
                            <span>
                              وضعیت حساب
                            </span>

                            <b
                              className={
                                profileData?.active
                                  ? 'profile-active'
                                  : 'profile-inactive'
                              }
                            >
                              {
                                profileData?.active
                                  ? 'فعال'
                                  : 'غیرفعال'
                              }
                            </b>
                          </div>


                          {
                            profileData?.employeeLinked
                            &&
                            <>
                              <div className="profile-divider"></div>

                              <div className="profile-detail-row">
                                <span>
                                  کد پرسنلی
                                </span>

                                <b dir="ltr">
                                  {profileData?.personnelCode ?? '—'}
                                </b>
                              </div>


                              <div className="profile-detail-row">
                                <span>
                                  کد ملی
                                </span>

                                <b dir="ltr">
                                  {profileData?.nationalCode ?? '—'}
                                </b>
                              </div>


                              <div className="profile-detail-row">
                                <span>
                                  واحد سازمانی
                                </span>

                                <b>
                                  {profileData?.department ?? '—'}
                                </b>
                              </div>
                            </>
                          }


                          {
                            !profileData?.employeeLinked
                            &&
                            <div className="profile-independent-note">
                              این حساب مدیریتی به پرونده پرسنلی مشخصی متصل نشده است.
                            </div>
                          }

                        </div>
                }


                <div className="profile-actions">

                  <a href="/admin/users">
                    مدیریت کاربران و دسترسی‌ها
                  </a>

                </div>

              </div>
            }

          </div>

        </div>
      </header>

      <section className="stats-grid">
        {liveStats.map(item=><StatCard key={item.title} {...item}/>)}
      </section>

      <section className="charts-grid">
        <DepartmentChart data={liveDepartments}/>
        <AttendanceTrendChart data={liveTrend}/>
        <AttendanceStatusChart data={liveStatus}/>
      </section>

      <section className="bottom-grid">
        <RecentAttendance rows={liveRecent}/>
        <PendingRequests rows={liveRequests}/>
        <div className="insight-column">
          <SmartAnalysis
            analysis={aiData?.analysis}
            available={aiData?.available}
            loading={aiLoading}
          />
          <ManagementAlerts alerts={liveAlerts}/>
        </div>
      </section>

      <footer className="dashboard-footer">
        © ۱۴۰۵ دانشگاه صنعتی شیراز — سامانه مدیریت حضور و غیاب
      </footer>
    </main>
  </div>
}
