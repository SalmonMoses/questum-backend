Вся документация и все(почти) примеры [здесь][https://documenter.getpostman.com/view/8084138/SWECYFfz]

[https://documenter.getpostman.com/view/8084138/SWECYFfz]: https://documenter.getpostman.com/view/8084138/SWECYFfz

### Список дел: ###
* Внутренний API:
    * - [x] Запрос всех администраторов
    * - [x] Запрос конкретного администратора
    * - [x] Проверка наличия администратора
    * - [x] Проверка наличия группы
    * - [x] Запрос всех групп
    * - [x] Запрос всех групп администратора
    * - [x] Запрос всех пользователей группы
    * - [x] Запрос конкретного пользователя
    * - [x] Запрос конкретной группы
    * - [x] Запрос всех квестов группы
    * - [x] Запрос конкретного квеста
    * - [ ] Получение всех подквестов по фильтру(группы, квеста, пройденные/непройденные)
    * - [ ] E-Mail при регистрации администратора
    * - [ ] E-Mail при добавлении нового пользователя в группу
    * - [ ] Работа с файлами Amazon S3
    * - [ ] Внешняя БД
* Администрирование:
    * - [x] Регистрация администратора группы
    * - [x] Логин в качестве администратора группы
    * - [x] Создание новой группы
    * - [x] Добавление нового пользователя в группу
    * - [x] Добавление нового квеста в группу
    * - [x] Получение турнирной таблицы группы
    * - [x] Изменение данных администратора
    * - [x] Изменение данных группы
    * - [x] Изменение квеста
    * - [x] Добавление подквестов
    * - [x] Удаление квеста
    * - [x] Удаление группы
    * - [x] Удаление администратора
    * - [ ] Изменение подквеста
    * - [ ] Изменение порядка подквестов в квесте
    * - [ ] Подтверждение прохождения подквеста
    * - [ ] Засчитывание прохождения квеста и начисление баллов
* Пользователь:
    * - [x] Логин в качестве пользователя
    * - [ ] Изменение информации о пользователе
    * - [ ] Отправление ответа на подквест

### Аутентификация в бэкэнде
Аутентификация(не логин, а аутентификация) запросов осуществляется в виде хедера Authorization вида 'Bearer [token]', где token - токен пользователя, полученный при логине/регистрации
