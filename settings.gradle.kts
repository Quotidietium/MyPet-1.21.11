rootProject.name = "MyPet"

include("api", "plugin", "skills")

// Quotidietium fork: 仅装配 v1_21_R7（服务器基线 1.21.11；其余 NMS 模块保留源码不入产物）
val nmsModules = listOf(":nms:v1_21_R7")
gradle.extra["nmsModules"] = nmsModules
include(nmsModules)
