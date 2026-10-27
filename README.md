# CodeDroid

Android-редактор кода в духе VS Code: файловый браузер, вкладки,
подсветка синтаксиса, маркетплайс плагинов VS Code, запуск JS.

## Сборка APK

1. Установите Android Studio (Ladybug+) с SDK 34.
2. Откройте папку CodeDroid как проект (Android Studio сама
   сгенерирует gradle wrapper и local.properties).
3. Build → Build APK(s) -> app-debug.apk будет в
   app/build/outputs/apk/debug/

## Ограничения

- Плагины .vsix скачиваются и распаковываются, но исполняемая
  часть (TypeScript, VS Code API) на Android не запускается.
  Совместимы: сниппеты, темы, грамматики подсветки.
- Python: подключите Chaquopy (chaquo.com/chaquopy).
