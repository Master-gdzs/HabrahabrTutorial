package ru.jdev.habrahabr;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Point2D;

import static java.lang.Math.random;
import static java.lang.Math.signum;
import static java.lang.Math.toRadians;

/**
 * User: Aleksey Zhidkov
 * Date: 17.07.12
 */
public class HabrahabrTutorial extends AdvancedRobot {

    private static final double RADIANS_5 = toRadians(5);

    private boolean isAlive = true;

    private double enemyX = -1;
    private double enemyY = -1;

    @Override
    public void run() {
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY); // пока противник не найден бесконечно крутим радар в право
        while (isAlive) { // в принципе это не обязательно и можно оставить true, не я предпочитаю избегать бесконечных циклов

            if (enemyX > -1) { // если противник обнаружен
                final double radarTurn = getRadarTurn();
                setTurnRadarRightRadians(radarTurn);

                final double bodyTurn = getBodyTurn();
                setTurnRightRadians(bodyTurn);

                if (getDistanceRemaining() == 0) {
                    final double distance = getDistance();
                    setAhead(distance);
                }

                final double gunTurn = getGunTurn();
                setTurnGunRightRadians(gunTurn);
                setFire(2);
            }

            /**
             * Вызовом этого метода робот сообщает движку, что он закончил вычисления и отдал все команды на текущий ход
             * Этот вызов блокируется до начала следующего кода
             */
            execute();
        }
    }

    private double getRadarTurn() {
        // роботу жизненно необходимо постоянно видеть противника
        // считаем абсолютный угол до противника:
        final double alphaToEnemy = angleTo(getX(), getY(), enemyX, enemyY);
        // считаем направление, на который надо повернуть радар, чтобы противник остался в фокусе:
        final double sign = (alphaToEnemy != getRadarHeadingRadians())
                ? signum(Utils.normalRelativeAngle(alphaToEnemy - getRadarHeadingRadians()))
                : 1;

        // добавляем 5 градусов поворта для надёжности и получаем результирующий угол
        return Utils.normalRelativeAngle(alphaToEnemy - getRadarHeadingRadians() + RADIANS_5 * sign);
        // В принципе, прямо здесь можно вызвать setTurnRadarRightRadians, но я противник функций с сайд эффектами и стараюсь
        // минимизировать их количество
    }

    private double getDistance() {
        // вычесление дистанции движения элементарно
        return 200 - 400 * random();
    }

    private double getBodyTurn() {
        // а вот вычисление угла поворота посложее
        final double alphaToMe = angleTo(enemyX, enemyY, getX(), getY());

        // определяем угловое направление относительно противника (по часовой стрелке, либо против) ...
        final double lateralDirection = signum((getVelocity() != 0 ? getVelocity() : 1) * Math.sin(Utils.normalRelativeAngle(getHeadingRadians() - alphaToMe)));
        // получаем желаемое направление движения
        final double desiredHeading = Utils.normalAbsoluteAngle(alphaToMe + Math.PI / 2 * lateralDirection);
        // нормализуем направление по скорости
        final double normalHeading = getVelocity() >= 0 ? getHeadingRadians() : Utils.normalAbsoluteAngle(getHeadingRadians() + Math.PI);
        // и возвращаем угол поворта
        return Utils.normalRelativeAngle(desiredHeading - normalHeading);
    }

   private double getGunTurn() {
       // вычисления тривиальны: считаем на какой угол надо повернуть пушку, чтобы она смотрела прямо на противника:
       return Utils.normalRelativeAngle(angleTo(getX(), getY(), enemyX, enemyY) - getGunHeadingRadians());
   }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        /** ScannedRobotEvent не содержит в себе явно положения противника, однако, его легко вычислить, зная направление
         * своего корпуса, беаринг (по сути угол относительный чего-то, в данном случае относительно корпуса) и расстояние до противника
         */

        // абсолютный угол до противника
        final double alphaToEnemy = getHeadingRadians() + event.getBearingRadians();

        // а далее элементарная геометрия
        enemyX = getX() + Math.sin(alphaToEnemy) * event.getDistance();
        enemyY = getY() + Math.cos(alphaToEnemy) * event.getDistance();
    }

    @Override
    public void onDeath(DeathEvent event) {
        isAlive = false;
    }

    @Override
    public void onPaint(Graphics2D g) {
        // убеждаемся, что вычислили позицию противника верно
        // для того чтобы увидеть что мы ресуем, необходимо во время битвы на правой понели кликнуть по имени робота
        // и в появившемся окне нажать кнопку Paint

        if (enemyX > -1) {
            g.setColor(Color.WHITE);
            g.drawRect((int) (enemyX - getWidth() / 2), (int) (enemyY - getHeight() / 2), (int) getWidth(), (int) getHeight());
        }
    }

    /**
     * В Robocode немного извращённые углы - 0 смотрит на север и далее по часовой стрелке:
     * 90 - восток, 180 - юг, 270 - запад, 360 - север.
     * <p/>
     * Из-за этого приходится писать собственный метод вычисления угла между двумя точками.
     * Вообще говоря, математика никогда не была моим коньком, поэтому, возможно, существует лучшее решение
     */
    private static double angleTo(double baseX, double baseY, double x, double y) {
        double theta = Math.asin((y - baseY) / Point2D.distance(x, y, baseX, baseY)) - Math.PI / 2;
        if (x >= baseX && theta < 0) {
            theta = -theta;
        }
        return (theta %= Math.PI * 2) >= 0 ? theta : (theta + Math.PI * 2);
    }

}
