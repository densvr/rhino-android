# rhino-android


Взяв за основу [rhino-android](https://github.com/F43nd1r/rhino-android), добавил
1. ZipFileAndroidClassLoader, для поддержки 4х андроидов
2. Запуск скрипта "1" + "1" в main activity для проверки
3. Выложил на jitpack и втянул в автору

Когда откажемся от 4х андроидов, вернемся на rhino-android

Проблема была с представлением dex файлов, библиотека rhino в райнтайме генерит dex файлы: javaScriptCompile -> bytecode -> dex file -> dalvik/art classLoader
Для 4х андроидов нужен особый формат classes.dex.zip
Те нужно положить dex файлы в zip архив
И потом считать классы с помощью DexClassLoader
