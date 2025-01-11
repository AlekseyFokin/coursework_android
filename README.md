### Skillcinema - финальная работа по курсу Android разработчик ###
Skillcinema - это android-приложения для поиска фильмов и сериалов, а также создания и управления своими коллекциями фильмов.
Источник данных для приложения — сервис Kinopoisk Api Unofficial.

**Пояснения к приложению:**

Приложение состоит из трех вкладок и BottomNavigationBar. Первая вкладка содержит готовые подборки фильмов.
Вторая вкладка - поиск фильмов по названию или актеру, с возможностью фильтрации. 
Третья вкладка - "Профиль", содержит собственные коллекции фильмов пользователя.  
При первом входе в приложение у пользователя есть возможность кратко познакомиться с приложением через коллекцию экранов Onboarding

**Подробности. Первая вкладка**

На вкладке "Главная" (HomeFragment) при открытии приложения загружаются 6 подборок фильмов размером 20 штук: премьеры,
популярное, две динамические коллекции, зависящие от комбинации страна/жанр, которые выбираются
случайным образом, топ-25 и сериалы - тоже загружаются из существующего пресета в api. 
Премьеры содержат коллекцию фильмов, которые выходят в прокат в ближайшие две недели. 
Временной интервал в 14 дней вычисляется при каждой загрузке этой коллекции.
Для загрузки динамических коллекций предварительно с api должны быть получены списки жанров и стран-производителей фильмов.

<img src="https://github.com/AlekseyFokin/coursework_android/blob/master/GIFS/load.gif" alt="gif" width="360" height="800">

Различные api дают похожую, но не одинаковую информацию о фильмах и ее приходится
приводить к общему отображаемому типу MovieRVModel, поэтому такое разнообразие классов в пакете
entities: премьеры, коллекции (топ-250, сериалы, популярные) и компиляции (зависят от страны и
жанра).

Чтобы на главном экране отображался список подборок в HomeFragment установлен двухуровневый RecyclerView с неоднородными viewHoldерами, который
реализован с помощью getItemViewType. RecyclerView должны заполнятся не только фильмами, но и дополнительными кнопками для открытия каждой подборки в отдельном фрагменте(PagingCollectionFragment,PagingCompilationFragment).
Идентификация вида viewHolder осуществляется по специальной метке в классе модели.

Все подборки фильмов, кроме премьер, загружаются во фрагментах PagingCollectionFragment,PagingCompilationFragment постранично и требуют paging lib, поэтому загружаются, минуя те usecase классы,
которые использовались для в HomeFragment. Вся обработка полученных данных делается в классах
MoviePagingSource. А при создании коллекций без пагинации, напротив, приведение загруженных данных
осуществляется в соответствующих usecase классах.

<img src="https://github.com/AlekseyFokin/coursework_android/blob/master/GIFS/home.gif" alt="gif" width="360" height="800">

Выбор - фрагмент с какой коллекцией/ компиляцией или премьерами загружать - выполняется в
HomeFragment - там обработка нажатия на кнопку "все" или "показать все". Дифференциация происходит
по стринговой метке, список которых лежит в KinopoiskApi.

На все переходы между фрагментами добавлена анимация на основе xml.

<img src="https://github.com/AlekseyFokin/coursework_android/blob/master/GIFS/one_movie_to_actor_info.gif" alt="gif" width="360" height="800">

При выборе любого фильма или сериала из подборки - открывается отельный фрагмент с информацией по фильму (OneMovieFragment):
- постером,
- названием,
- рейтингом,
- описанием фильма,
- меню добавления фильмов в коллекции,
- списком снимавшихся в фильме актеров (ограничен 20),
- списком, работавших над фильмом кинематографистов(ограничен 20),
- галереей - списком изображений к фильму,
- списком похожих фильмов.

Последние 4 пункта - ограниченные списки, каждый из них можно открыть в отдельном фрагменте и загрузить полную информацию из api.
При этом, например список изображений к фильму разбит на различные категории то как: постеры, фото со съемочной площадки и т.д.

Списки актеров и кинематографистов открываются в схожих фрагментах и позволяют при выборе актера получить по нему всю известную информацию на отдельном фрагменте MoviemanFragment.
Такой фрагмент содержит:
- фотографию актера или кинематографиста,
- имя,
- список с 10 лучшими фильмами по версии kinopoisk.ru
- фильмографию, разбитую по категориям (актер, режиссер, сценарист и т.п.)
<img src="https://github.com/AlekseyFokin/coursework_android/blob/master/GIFS/serial_info.gif" alt="gif" width="360" height="800">

Из этих списков можно выбрать любой фильм и опять попасть типовой фрагмент с информацией по фильму, а из него снова выбрать интересного актера и перейти на фрагмент с информацией о нем и т.д., при этом вся цепочка поисков будет сохранятся в бэкстеке и всегда можно будет вернуться по ней. 

**Подробности. Вторая вкладка**

Закладка "Поиск" содержит поле символьного поиска и кнопку перехода в конструктор фильтров.
Фильтровать можно по типу (фильм/сериал), стране, жанру, рейтингу и году выхода.
Фильтр представлен несколькими фрагментами, но они используют разделяемую ViewModel. 
Для выбора года выпуска фильма используется разработанный мной CastomView.
Каждый результат поиска можно открыть в отельном фрагменте с полным функционалом просмотра информации о фильме (OneMovieFragment).

<img src="https://github.com/AlekseyFokin/coursework_android/blob/master/GIFS/search.gif" alt="gif" width="360" height="800"> 

**Подробности. Третья вкладка**

Вкладка "Профиль" позволяет пользователю приложения создавать свои коллекции фильмов,и наполнять встроенные коллекции, такие как "Любимые фильмы", "Хочу просмотреть", "Просмотрено".
Все коллекции хранятся в БД sqlite. Работа с БД осуществляется через ROOM. 
База данных состоит из двух связанных таблиц. В одной хранятся записи коллекций, во второй
информация о фильмах. Фильмы могут принадлежать нескольким коллекциям, при этом записей с одним
фильмом будет столько, во скольких коллекциях он присутствует. В таблице фильмов хранятся пути к
постерам фильмов. Сами постеры (jpg файлы) сохраняются в директорию posters во внутренней памяти
приложения, поэтому разрешения у пользователя не просят. При удалении фильма из коллекции - файл с
постером тоже удаляется. Фильм, который был открыт в OneMovieFragment добавляется в коллекцию
фильмов, которыми пользователь интересовался, при этом размер этой коллекции - 20 фильмов, дубли не
добавляются, при превышении порога самая старая запись - удаляется. При создании БД в нее вносятся
предустановленные коллекции, которые нельзя потом удалить (поле embdded). Новые коллекции, которые
создает пользователь можно удалять вместе с фильмами в них хранящимися.

Наиболее интересный механизм синхронизации фильмов которые попали в коллекцию просмотренных,
ведь они во всех RecyclerView по всему приложению должны сразу же получить метку в правом нижнем углу(глаз) и быть залиты
сине-прозрачным градиентом. Поэтому требуется передавать сигнал по обновлению RecyclerView в предыдущие
фрагменты(из бекстека). По - возможности во RecyclerView обновляются отдельные записи, но такое не
везде возможно, например во фрагменте HomeFragment один и тот же фильм вполне может быть сразу в
нескольких RecyclerView поэтому при получении этим фрагментом сигнала об изменении состояния фильма - он
полностью перезагружается. Для передачи таких сигналов об обновлении состояния фильма используется FragmentResult.
В месте указания что фильм просмотрен создается результат фрагмента, а в прочих фрагментах из бэкстека на функцию onResume вешается листнер
FragmentResult. Этот же листнер при необходимости прокидывает результат на следующий
восстанавливаемый из бекстека фрагмент.

Добавление фильма в коллекции осуществляется во фрагменте просмотра информации о фильме (OneMovieFragment). Там же можно создавать свои коллекции в отдельном BottomSheetDialogFragment.
Во вкладке "Профиль" коллекции можно просматривать, создавать и удалять.

<img src="https://github.com/AlekseyFokin/coursework_android/blob/master/GIFS/work_with_db_collections.gif" alt="gif" width="360" height="800"> 

**Подробности. Дополнительная информация**

Такой большой проект невозможно было наполнить зависимостями в ручную и, конечно, для этого используется DI библиотека Hilt.
Для наблюдением за появляющимися ошибками к приложению подключен Firebase Crashlytics
Для более подробного знакомства с приложением можно обратиться к тех.заданию и figma-макету по адресу https://www.figma.com/design/IIkauEDQXNEAbzLtBufGOj/skillcinema-(Copy)?m=auto&t=F5O6r2FkJLGH71jL-1
