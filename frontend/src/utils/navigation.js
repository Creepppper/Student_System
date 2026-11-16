// 仅决定登录入口和菜单展示，访问权限仍由后端控制。
export function defaultEntry(roles) {
  if (roles.includes('admin')) return '/system/user'
  if (roles.includes('teacher')) return '/system/student'
  if (roles.includes('student')) return '/student/myscore'
  return '/401'
}

export function visibleMenus(menus, roles) {
  return menus.filter(route => route.path !== '/tool' &&
    (route.path !== '/student' || roles.includes('student')))
}

export function flattenSystemMenu(routes) {
  return routes.flatMap(route => route.path === '/system' && route.children
    ? route.children.map(child => ({ ...child, path: `/system/${child.path}` }))
    : [route])
}
