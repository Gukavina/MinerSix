import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.border.Border;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Базовый класс приложения
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public class MinerSix extends JFrame {
    // Точка входа
    public static void main(String args[]) {
        // Создать стартовое окно
        new MinerSix();
    }
    // Окно запуска программы (конструктор класса)
   private MinerSix() {
        JDialog dialogStart = new JDialog();
        dialogStart.setBounds(START_LOCATION,START_LOCATION,320,360);
        dialogStart.setTitle("Параметры игры");
        dialogStart.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Создать панель и добавить её в окно диалога
        JPanel panelDialogStart = new JPanel();
        panelDialogStart.setSize(320 ,360);
        dialogStart.add(panelDialogStart);

        // Создать кнопку запуска игры
        JButton bootStart = new JButton();
        bootStart.setText("Играть");
        //bootStart.setBounds(50,195, 100, 30);

        // Создать ловушку "клика" для кнопки "Играть"
        bootStart.addMouseListener(new MouseAdapter(){ public void mouseClicked( MouseEvent event) {
            clickButtStart(dialogStart);
        }});

        // Добавить элементы управления на панель
        panelDialogStart.add(bootStart);

       // Создать кнопку запуска включения искуственного интелекта
       JButton bootAI = new JButton();
       bootAI.setText("Включить ИИ");
       //bootAI.setBounds(50,195, 100, 30);

       // Создать ловушку "клика" для кнопки "Играть"
       bootAI.addMouseListener(new MouseAdapter(){ public void mouseClicked( MouseEvent event) {
           clickButtAI(bootAI);
       }});

       // Добавить элементы управления на панель
       panelDialogStart.add(bootAI);

        // Запустить окно диалога
        dialogStart.setVisible(true);
    }

    // Обработчик кнопки старт
    private void clickButtStart(JDialog dialogStart) {
        dialogStart.dispose();
        new MinerSixField();
    }

    // Обработчик кнопки включения ИИ
    private void clickButtAI(JButton bootAI) {
        if(AIntelligenceFlag) {
            AIntelligenceFlag = false;
            bootAI.setText("Включить ИИ");
        }
        else {
            AIntelligenceFlag = true;
            bootAI.setText("Выключить ИИ");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Окно игрового поля
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class MinerSixField  extends JFrame {
        // Конструктор класса
        private MinerSixField(){
            // Определение параметров окна игрового поля
            setTitle("Сапёр шестигранный");
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setBounds(START_LOCATION+50, START_LOCATION+50, fieldW, fieldH);
            setResizable(false);

            // Создать панели для позиционирования элементов интерфейса
            JPanel	panelRoot	=	new JPanel();
            JPanel	panelTimer	=	new JPanel();
            panelFiled  =   new Canvas();

            // Добавить корневую панель
            setContentPane(panelRoot);
            panelRoot.setLayout(new BorderLayout());

            // Создать канву для игрового поля и добавить её на панель
            Border CanvasText = BorderFactory.createEtchedBorder();
            panelFiled.setBorder(CanvasText);
            panelRoot.add(BorderLayout.CENTER, panelFiled);

            // Создать игровой таймер и добавить его на панель
            panelRoot.add(panelTimer,BorderLayout.SOUTH);
            final TimerLabel timeLabel = new TimerLabel();
            Border TimerText = BorderFactory.createEtchedBorder();
            panelTimer.setBorder(TimerText);
            panelTimer.add(BorderLayout.SOUTH, timeLabel);

            // Создать игру
            new MinerSixGame(panelFiled);

            // Запустить окно игрового поля
            setVisible(true);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Канва для рисования игрового поля
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            // Вызываем родительский метод отрисовки
            super.paint(g);
            // Рисуем ячейки
            g.setColor(Color.lightGray);
            for (int x = 0; x < fieldSizeX; x++)
                for (int y = 0; y < fieldSizeY; y++) {
                    // Нарисовать полигоны
                    g.drawPolygon(arrayPoligynDraw[x][y]);
                    if(arrayFill[x][y]) g.fillPolygon(arrayPoligynFill[x][y]);
                    // Перерисовать флаги
                    setCellFlag(g, x, y);
                    // Перерисовать мины
                    setCellBomb(g, x, y);
                    // Отобразить количество мин
                    setCellNumBomb(g, x, y);
                }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Игровой таймер
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class TimerLabel extends JLabel {
        java.util.Timer timer = new Timer();

        //конструктор
        TimerLabel() { timer.scheduleAtFixedRate(timerTask, 0, 1000); }

        TimerTask timerTask = new TimerTask() {
            volatile int time;
            Runnable refresher = new Runnable() {
                public void run() {
                    TimerLabel.this.setText(String.format("%02d:%02d", time / 60, time % 60));
                }
            };
            public void run() {
                time++;
                SwingUtilities.invokeLater(refresher);
            }
        };

        //void stopTimer() { timer.cancel(); }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Игровой класс
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class MinerSixGame  {
        // Конструктор класса
        private MinerSixGame(Canvas  panelFiled) {
            initCell();
            // Создать ловушку "клика" мышки для игрового поля
            panelFiled.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent event) {
                // Определить выбранную ячейку
                clickFieldGames(event);
            }});
        }
        // Обработчик "клика" на игровом поле
        private void clickFieldGames(MouseEvent event){
            // Кооординаты "клика" мышки
            int mouseX = event.getX();
            int mouseY = event.getY();
            buttonMouseClick = event.getButton();
            // Флаг для "ускоренного" выхода из цикла
            boolean flagClikFieldGames = false;
            // Определить выбранный блоки
            for (int x = 0; x < fieldSizeX; x++) {
                for (int y = 0; y < fieldSizeY; y++) {
                    if(arrayPoligynDraw[x][y].contains(mouseX, mouseY)) {
                        currentX = x;
                        currentY = y;
                        flagClikFieldGames = true;
                        // Если нажата левая кнопка мыши
                        if((buttonMouseClick == 3)&&(arrayFill[x][y])) {
                            // Изменить флаг на противоположный
                            arrayFlags[x][y] ^= true;
                            // Подсчитать количества отмеченных мин
                            if(arrayFlags[x][y] && arrayBomb[x][y]) numOpenBomb++;
                            if((!arrayFlags[x][y]) && arrayBomb[x][y]) numOpenBomb--;
                        }
                        setCellFlag(panelFiled.getGraphics(), x, y);
                        // Если нажата правая кнопка мыши
                        if((buttonMouseClick == 1)&&(!arrayFlags[x][y])) {
                            // Открыть ячейку
                            openCells(x, y);
                            // Проверить условия окончания игры
                            //gamesOver();
                        }
                    }
                    if(flagClikFieldGames) break;
                }
                if(flagClikFieldGames) break;
            }
            // Проверить условия окончания игры
            gamesOver();

            // Перерисовать конву
            panelFiled.repaint();


            // Вывод тестовых сообщений
            //System.out.printf("Координаты \"клика\":  x = %d; y = %d.\n", mouseX, mouseY);
            //System.out.printf("Координаты \"ячейки\": x = %d; y = %d.\n", currentX, currentY);
            //System.out.println(arrayFlags[currentX][currentY]);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Окно окончания игры
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class MinerSixGameOver extends JFrame {

        // Конструктор класса
        private MinerSixGameOver() {
            JDialog dialogStart = new JDialog();
            dialogStart.setBounds(START_LOCATION, START_LOCATION, 320, 100);
            //System.out.println("Выбрана ячейка с миной! ВЫ ПРОИГРАЛИ!");
            if(gameOverFlag)    dialogStart.setTitle("Игра окончена! Вы выиграли!");
            else                dialogStart.setTitle("Игра окончена! Вы проиграли!");
            dialogStart.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            // Создать панель и добавить е в окно диалога
            JPanel panelDialogStart = new JPanel();
            panelDialogStart.setSize(320, 360);
            dialogStart.add(panelDialogStart);

            // Создать кнопку запуска игры
            JButton bootStart = new JButton();
            bootStart.setText("Новая игра");
            //bootStart.setBounds(50, 195, 150, 30);

            // Создать ловушку "клика" для кнопки "Новая игра"
            bootStart.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent event) {
                    clickButtStart(dialogStart);
                }
            });

            // Добавить элементы управления на панель
            panelDialogStart.add(bootStart);

            // Создать кнопку запуска включения искуственного интелекта
            JButton bootAI = new JButton();
            //bootAI.setText("Включить ИИ");
            //bootAI.setBounds(50,195, 100, 30);
            if(AIntelligenceFlag)   bootAI.setText("Выключить ИИ");
            else                    bootAI.setText("Включить ИИ");

            // Создать ловушку "клика" для кнопки "Играть"
            bootAI.addMouseListener(new MouseAdapter(){ public void mouseClicked( MouseEvent event) {
                clickButtAI(bootAI);
            }});

            // Добавить элементы управления на панель
            panelDialogStart.add(bootAI);

            // Запустить окно диалога
            dialogStart.setVisible(true);
        }

        // Обработчик кнопки старт
        private void clickButtStart(JDialog dialogStart) {
            //
            dialogStart.dispose();

            //
            initCell();

            //
            panelFiled.repaint();

        }

        // Обработчик кнопки включения ИИ
        private void clickButtAI(JButton bootAI) {
            if(AIntelligenceFlag) {
                AIntelligenceFlag = false;
                bootAI.setText("Включить ИИ");
            }
            else {
                AIntelligenceFlag = true;
                bootAI.setText("Выключить ИИ");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Глобальные метода
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Функция инициализации геометрии полигона
    private static void initPoligyn(int x, int y) {
        // Масштабирующие и смещающие коэффициенты
        int dX  = arr20*BLOCK_SIZE;   // Смещение между ячейками по X
        int dY  = arr15*BLOCK_SIZE;   // Смещение между ячейками по Y
        int dS  = arr10*BLOCK_SIZE;   // Смещение между строками поля

        // Создать массив ячеек
        arrayPoligynDraw[x][y] = new Polygon();
        arrayPoligynFill[x][y] = new Polygon();

        // Определение координат ячейки
        for (int i = 0; i < nP; i++) {
            arrayX1[i] = arrayX0[i]*BLOCK_SIZE + dX*x + (y%2)*dS;
            arrayY1[i] = arrayY0[i]*BLOCK_SIZE + dY*y;
        }
        // Инициализация полигона
        arrayPoligynDraw[x][y].addPoint(arrayX1[0], arrayY1[0]);
        arrayPoligynDraw[x][y].addPoint(arrayX1[1], arrayY1[1]);
        arrayPoligynDraw[x][y].addPoint(arrayX1[2], arrayY1[2]);
        arrayPoligynDraw[x][y].addPoint(arrayX1[3], arrayY1[3]);
        arrayPoligynDraw[x][y].addPoint(arrayX1[4], arrayY1[4]);
        arrayPoligynDraw[x][y].addPoint(arrayX1[5], arrayY1[5]);
        arrayPoligynDraw[x][y].addPoint(arrayX1[6], arrayY1[6]);

        // Определение координат ячейки
        for (int i = 0; i < nP; i++) {
            arrayX1[i] += arrayXD[i];
            arrayY1[i] += arrayYD[i];
        }
        // Инициализация полигона
        arrayPoligynFill[x][y].addPoint(arrayX1[0], arrayY1[0]);
        arrayPoligynFill[x][y].addPoint(arrayX1[1], arrayY1[1]);
        arrayPoligynFill[x][y].addPoint(arrayX1[2], arrayY1[2]);
        arrayPoligynFill[x][y].addPoint(arrayX1[3], arrayY1[3]);
        arrayPoligynFill[x][y].addPoint(arrayX1[4], arrayY1[4]);
        arrayPoligynFill[x][y].addPoint(arrayX1[5], arrayY1[5]);
        arrayPoligynFill[x][y].addPoint(arrayX1[6], arrayY1[6]);
    }
    // Инициализация параметров ячейки игрового поля
    private void initCell() {
        // Локальные переменные
        int x, y; // Счётчики циклов
        // Начальное заполнение массивов
        for (x = 0; x < fieldSizeX; x++)
            for (y = 0; y < fieldSizeY; y++) {
                // Инициализация геометрии ячеек
                initPoligyn(x, y);
                // Начальная инициализация массивов флагов
                arrayFlags[x][y] = false;
                arrayFill[x][y]  = true;
                arrayBomb[x][y]  = false;
                // Определение центра яцеек
                centerCellX[x][y] = (x*arr20+arr10+(y%2)*arr10)*BLOCK_SIZE;
                centerCellY[x][y] = (y*arr15+arr10)*BLOCK_SIZE;
            }

        // Случайная расстановка мин
        int indX;
        int indY;
        int countMines = 0;
        while (countMines < numberBomb) {
            indX = random.nextInt(fieldSizeX);
            indY = random.nextInt(fieldSizeY);
            if(!arrayBomb[indX][indY]) {
                arrayBomb[indX][indY] = true;
                countMines++;
            }
        }

        // Определение количество мин фокруг ячейки
        for(y = 0; y < fieldSizeY; y++) for(x = 0; x < fieldSizeX; x++) if(arrayBomb[x][y]) {
            // Сзади
            indX = x-1;
            indY = y;
            if(indX >= 0) arrayNumBomb[indX][indY]++;
            // Спереди
            indX = x+1;
            indY = y;
            if(indX < fieldSizeX) arrayNumBomb[indX][indY]++;
            // Сзади и вверху
            indX = x-1+(y%2);
            indY = y-1;
            if((indX >= 0) && (indY >= 0)) arrayNumBomb[indX][indY]++;
            // Спереди и вверху
            indX = x+(y%2);
            indY = y-1;
            if((indX < fieldSizeX) && (indY >= 0)) arrayNumBomb[indX][indY]++;
            // Внизу и сзади
            indX = x-1+(y%2);
            indY = y+1;
            if((indX >= 0) && (indY < fieldSizeY)) arrayNumBomb[indX][indY]++;
            // Внизу и спереди
            indX = x+(y%2);
            indY = y+1;
            if((indX < fieldSizeX) && (indY < fieldSizeY)) arrayNumBomb[indX][indY]++;
        }

        // Количество открытых мин
        numOpenBomb = 0;
    }

    // Метод рисование символа
    private void paintString(Graphics g, String str, int x, int y, Color color) {
        Color colorCurrier = g.getColor();
        g.setColor(color);
        g.setFont(new Font("", Font.BOLD, FONT_SIZE));
        g.drawString(str, x, y);
        g.setColor(colorCurrier);
    }

    // Метод отображения флага
    private void setCellFlag(Graphics g, int x, int y) {
        // Определить условие отображения флага
        //boolean flag   = arrayFlags[x][y];
        // Определить координаты вывода флага
        int indxX = centerCellX[x][y] - FONT_SIZE/4;
        int indxY = centerCellY[x][y] + FONT_SIZE/3;
        // Определить символ флага
        String str = "f";
        // "Нарисовать" флаг на игровом поле
        if(arrayFlags[x][y]) paintString(g,str, indxX, indxY, Color.red);
    }

    // Метод отображения мин
    private void setCellBomb(Graphics g, int x, int y) {
        // Определить условие отображения флага
        boolean flag   =  (!arrayFill[x][y])&&(arrayBomb[x][y]);
        // Определить координаты вывода флага
        int indxX = centerCellX[x][y];
        int indxY = centerCellY[x][y];
        // Определить цвет мины
        Color colorBomb = ((!gameOverFlag) && (x == currentX) && (y == currentY)) ? Color.red : Color.black;
        // "Нарисовать" мину на игровом поле
        if(flag) paintBomb(g, indxX, indxY, colorBomb);
    }

    // Метод рисования мины
    private void paintBomb(Graphics g, int x, int y, Color color) {
        // Сохранить цвет
        Color colorCurrier = g.getColor();
        // Установить цвет
        g.setColor(color);
        // Нарисовать мину
        g.fillRect(x-4*BLOCK_SIZE,y-2*BLOCK_SIZE,8*BLOCK_SIZE,4*BLOCK_SIZE);
        g.fillRect(x-2*BLOCK_SIZE,y-4*BLOCK_SIZE,4*BLOCK_SIZE,8*BLOCK_SIZE);
        g.fillRect(x-3*BLOCK_SIZE,y-3*BLOCK_SIZE,6*BLOCK_SIZE,6*BLOCK_SIZE);
        g.setColor(Color.white);
        g.fillRect(x-2*BLOCK_SIZE,y-2*BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
        // Восстановить цвет
        g.setColor(colorCurrier);
    }

    // Метод отображения количества мин вокруг ячейки
    private void setCellNumBomb(Graphics g, int x, int y) {
        // Определить условие отображения флага
        boolean flag = (!arrayBomb[x][y])&&(arrayNumBomb[x][y]!=0)&&(!arrayFill[x][y]);
        //boolean flag = (!arrayBomb[x][y])&&(!arrayFill[x][y]);
        // Определить координаты вывода флага
        int indxX = centerCellX[x][y] - FONT_SIZE/4;
        int indxY = centerCellY[x][y] + FONT_SIZE/3;
        // Определить символ флага
        String str = Integer.toString(arrayNumBomb[x][y]);
        // Определить цвет
        Color color = Color.red;
        int numBomb = arrayNumBomb[x][y];
        //if(numBomb == 0) color = Color.gray;
        if(numBomb == 1) color = Color.gray;
        if(numBomb == 2) color = Color.green;
        if(numBomb == 3) color = Color.blue;
        if(numBomb == 4) color = Color.orange;
        // Отобразить количество мин вокруг ячейки на игровом поле
        if(flag) paintString(g,str, indxX, indxY, color);
    }

    // Метод, обеспечивающий открытие пустых ячеек
    private void openCells(int x, int y) {
        // Выйти, если координата за границами поля
        if((x < 0) || (x > fieldSizeX - 1) || (y < 0) || (y > fieldSizeY - 1)) return;
        // Выйти, если ячейка открыта
        if(!arrayFill[x][y]) return;
        // Выйти, если в ячейке мина
        if(arrayBomb[x][y]) return;
        // Открыть ячейку
        arrayFill[x][y] = false;
        // Выйти, если ячейка не нулевая
        if(!AIntelligenceFlag) if(arrayNumBomb[x][y] != 0) return;
        // Выбрать новую ячейку
        for (int dx = -1; dx < 2; dx++)
            for (int dy = -1; dy < 2; dy++) {
                if(dy != 0) dx += (y%2);
                openCells(x + dx, y + dy);
            }
    }

    // Метод окончания игры
    private void gamesOver() {
        //System.out.println("Проверка условия выхода из игры");
        //System.out.printf("Координаты \"ячейки\": x = %d; y = %d.\n", currentX, currentY);
        //System.out.printf("Количество помеченных мин: %d\n", numOpenBomb);
        int x = currentX;
        int y = currentY;
        if(arrayBomb[x][y] && (buttonMouseClick == 1)) {
            //System.out.println("Выбрана ячейка с миной! ВЫ ПРОИГРАЛИ!");
            gameOverFlag = false;
            for(int indX = 0; indX < fieldSizeX; indX++) for(int indY = 0; indY < fieldSizeY; indY++) {
                arrayFill[indX][indY]    = false;
                arrayFlags[indX][indY]   = false;
                arrayNumBomb[indX][indY] = 0;
            }
            // Создать стартовое окно
            new MinerSixGameOver();
        }
        else {
            if(numOpenBomb == numberBomb) {
                //System.out.println("Все мины отмечены! ВЫ ВЫИГРАЛИ!");
                gameOverFlag = true;
                for(int indX = 0; indX < fieldSizeX; indX++) for(int indY = 0; indY < fieldSizeY; indY++) {
                    arrayFill[indX][indY]    = false;
                    arrayFlags[indX][indY]   = false;
                    arrayNumBomb[indX][indY] = 0;
                }
                // Создать стартовое окно
                new MinerSixGameOver();
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Глобальные параметры
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //private MinerSixGame minerSix;

    private static int START_LOCATION = 50; // Стартовая координата окна
    private static int fieldSizeX = 9;      // Количество ячеек по горизонтали
    private static int fieldSizeY = 9;      // Количество ячеект по вертикали
    private static int BLOCK_SIZE = 3;      // Условный размер ячейки
    private static int FONT_SIZE  = 30;     // Размер шрифта для канвы
    private static int arr05 = 5;
    private static int arr10 = 10;
    private static int arr15 = 15;
    private static int arr20 = 20;
    private static int fieldW  = (arr20*fieldSizeX+arr05)*BLOCK_SIZE+24;   // Ширина игрового поля
    private static int fieldH  = (arr15*fieldSizeY+arr05)*BLOCK_SIZE+72;   // Высота игрового поля

    // Массивы, константы и пр. объекты, определяющие форму ячейки
    private static int nP = 7; // Количество вершин ячейки + 1
    private static int arrayX0[] = {arr10,arr20,arr20,arr10,0,0,arr10};
    private static int arrayY0[] = {0,arr05,arr15,arr20,arr15,arr05,0};
    private static int arrayX1[] = new int[nP];
    private static int arrayY1[] = new int[nP];
    private static int arrayXD[] = {0,-1,-1, 0, 1, 1, 0};
    private static int arrayYD[] = {2, 1,-1,-2,-1, 1, 2};
    private static int currentX  = 0;
    private static int currentY  = 0;
    private static int centerCellX[][] = new int[fieldSizeX][fieldSizeY];
    private static int centerCellY[][] = new int[fieldSizeX][fieldSizeY];

    // Массивы и пр. объекты для случайного размещения мин
    private Random random = new Random();                                   // Генератор случайных чисел
    private int numberBomb = 10;                                            // Моличество мин
    private boolean arrayBomb[][] = new boolean[fieldSizeX][fieldSizeY];    // Массив флагов наличие мин в ячейках
    private int arrayNumBomb[][]  = new int[fieldSizeX][fieldSizeY];        // Массив с количеством мин вокруг ячейки

    // Панель для отрисовки игрового поля
    private Canvas  panelFiled;

    //final int arrayCollor[] = {0x0000FF, 0x008000, 0xFF0000, 0x800000, 0x0}; // Цвета мин и цифр

    // Массив полигонов для игрового поля
    private static Polygon arrayPoligynDraw[][] = new Polygon[fieldSizeX][fieldSizeY];
    private static Polygon arrayPoligynFill[][] = new Polygon[fieldSizeX][fieldSizeY];

    // Массивы флагов
    private static boolean arrayFlags[][] =  new boolean[fieldSizeX][fieldSizeY];
    private static boolean arrayFill[][]  =  new boolean[fieldSizeX][fieldSizeY];

    // Флаг искуственного интелекта
    private boolean AIntelligenceFlag = false;

    // Конец игры
    //private static int gameOverX = 0;
    //private static int gameOverY = 0;
    private boolean gameOverFlag = false;   // Результаты игры
    private int numOpenBomb = 0;            // Количество открытых бомб
    private int buttonMouseClick = 0;       // Номер нажатой кнопки мыши
}
