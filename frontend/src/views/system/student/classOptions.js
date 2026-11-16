export function classFilterOptions(grades, classes, assignments, isAdmin, grade) {
  if (isAdmin) return { grades, classes }
  return {
    grades: grades.filter(item => assignments.some(assignment => assignment.grade === item)),
    classes: classes.filter(item => assignments.some(assignment => assignment.grade === grade && assignment.className === item))
  }
}
