# 保持 Room 实体类不被混淆
-keep class com.today.habit.data.entity.** { *; }

# 保持备份数据模型不被混淆
-keep class com.today.habit.ui.viewmodel.BackupData { *; }

# 保持 Gson 相关注解
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
