# 上游源码记录

记录日期：2026-10-23。

| 部分 | 仓库 | 分支 | Commit SHA | 导入方式 |
| --- | --- | --- | --- | --- |
| 后端 | `https://github.com/yangzongzhuan/RuoYi-Vue.git` | `springboot3` | `a51a838b71b446ea27256900efe7ed2faa2a02fd` | 临时浅克隆后以 `git archive` 导入项目根目录 |
| 前端 | `https://github.com/yangzongzhuan/RuoYi-Vue3.git` | `master` | `838965c5a18d2c61b73ec30c6e288057aaa08b63` | 临时浅克隆后以 `git archive` 导入 `frontend/` |

导入过程没有复制任一上游 `.git` 目录，因此项目内不存在嵌套仓库。根目录保留后端 `LICENSE`，`frontend/LICENSE` 保留前端许可证。

计划中的 Git remote：

```bash
git remote add upstream https://github.com/yangzongzhuan/RuoYi-Vue.git
```

当前工作区提供了只读空 `.git/` 占位目录，Git 无法识别或初始化仓库，因此 remote 尚未实际登记。解除该工作区限制后再执行上述命令；`origin` 应指向小组仓库，不得指向官方仓库。
