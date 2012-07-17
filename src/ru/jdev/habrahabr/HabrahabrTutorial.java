package ru.jdev.habrahabr;

import robocode.AdvancedRobot;

/**
 * User: Aleksey Zhidkov
 * Date: 17.07.12
 */
public class HabrahabrTutorial extends AdvancedRobot {

    @Override
    public void run() {
        while (true) {
            /**
             * Вызовом этого метода робот сообщает движку, что он закончил вычисления и отдал все команды на текущий ход
             * Этот вызов блокируется до начала следующего кода
             */
            execute();
        }
    }
}
