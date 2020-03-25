# rhino-android


Взяв за основу [rhino-android](https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=2ahUKEwjgvITP7LXoAhUl86YKHf9YAk4QFjAAegQIBRAB&url=https%3A%2F%2Fgithub.com%2FF43nd1r%2Frhino-android&usg=AOvVaw0h2tnQYM-QlkJMBJ-EtD3H), добавил 
1. ZipFileAndroidClassLoader, для поддержки 4х андроидов
2. Запуск скрипта "1" + "1" в main activity для проверки
3. Выложил на jitpack и втянул в автору

Когда откажемся от 4х андроидов, вернемся на rhino-android

Проблема была с представлением dex файлов, библиотека rhino в райнтайме генерит dex файлы: javaScriptCompile -> bytecode -> dex file -> dalvik/art classLoader
Для 4х андроидов нужен особый формат classes.dex.zip
Те нужно положить dex файлы в zip архив
И потом считать классы с помощью DexClassLoader
