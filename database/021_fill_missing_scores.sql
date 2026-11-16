-- 在 001 建表及现有数据导入之后执行。执行前备份 stu_score，以便恢复原来的空值。
-- 不改变缺考标记；0 分不一定表示缺考。重复执行不会改动已有成绩。
UPDATE stu_score
SET chinese = COALESCE(chinese, 0), math = COALESCE(math, 0),
    english = COALESCE(english, 0), physics = COALESCE(physics, 0),
    chemistry = COALESCE(chemistry, 0), biology = COALESCE(biology, 0)
WHERE chinese IS NULL OR math IS NULL OR english IS NULL
   OR physics IS NULL OR chemistry IS NULL OR biology IS NULL;
