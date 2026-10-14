UnstableSharksPvP v1.1.0 — Paper 1.21.11

المميزات:
- /pvpstats إحصائيات القتل والموت ونسبة KDR والقتل المتتالي
- /uspvp reload لإعادة تحميل الإعدادات (للإدارة)
- حفظ الإحصائيات في plugins/UnstableSharksPvP/stats.yml
- منع الضرر المباشر ومقذوفات الوحوش، ومنع ظهور الوحوش الطبيعي (حسب العالم)
- رسائل عند الوصول إلى Killstreak محدد

التوافق:
- PlayerKits2 مسؤول عن الكتات (هذا البلوقن لا يستبدله)
- CombatLogX مسؤول عن Combat Tag (هذا البلوقن لا يستبدله)
- WorldGuard مسؤول عن حماية السبون
- لا يوجد تكامل TAB/PAPI حتى الآن

طريقة البناء:
1. ثبّت Java 21 و Maven 3.9 أو أحدث.
2. من مجلد المشروع نفّذ: mvn clean package
3. الملف الناتج target/UnstableSharksPvP-1.1.0.jar ارفعه إلى plugins في Minehut.
4. أعد تشغيل السيرفر، ثم عدّل config.yml حسب اسم عالمك.

ملاحظة: هذه حزمة مصدر، وليست JAR مبنيًا أو مجرّبًا على سيرفر فعلي.
