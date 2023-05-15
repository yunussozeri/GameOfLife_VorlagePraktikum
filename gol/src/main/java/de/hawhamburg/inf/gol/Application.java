package de.hawhamburg.inf.gol;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Main application class.
 *
 * @author Christian Lins
 */
public class Application {

    /* Size of the playground in X dimension */
    public static final int DIM_X = 200;

    /* Size of the playground in Y dimension */
    public static final int DIM_Y = 200;

    /* Probability threshold that a cell is initially being created */
    public static final float ALIVE_PROBABILITY = 0.3125f;

    /* Sleep time between every generation in milliseconds */
    public static final int SLEEP = 200;

    /**
     * Creates an potentially unlimited stream of Cell objects. The stream uses
     * random numbers between [0, 1] and the probability threshold whether a
     * cell is created DEAD (random > p) or ALIVE (random <= p).
     *
     * @param p Cell alive probability threshold.
     * @return
     */
    private static Stream<Cell> createCellStream(float p) {
        Preconditions.checkArgument(p > 0 && p < 1, "p ist nicht zwischen 0 und 1", p);

        // generate a stream of random numbers between 0 and 1
        Stream<Double> randf = Stream.generate(Math::random);

        // create  dead cells if the random > p
        // create alive cells if the random <= p
        // map function transforms the random numbers to cell objects.
        Stream<Cell> cellstream = randf.map(random -> new Cell(random <= p ? 1 : 0));

        return cellstream;
    }

    public static void main(String[] args) {
        Stream<Cell> cellStream = createCellStream(ALIVE_PROBABILITY);
        Playground playground = new Playground(DIM_X, DIM_Y, cellStream);

        // Create and show the application window
        ApplicationFrame window = new ApplicationFrame();
        window.setVisible(true);
        window.getContentPane().add(new PlaygroundComponent(playground));

        // Create and start a LifeThreadPool with 50 threads
        LifeThreadPool pool = new LifeThreadPool(50);
        pool.start();
        
        while (true) {
            Life life = new Life(playground);
            synchronized (pool) {
                for (int xi = 0; xi < DIM_X; xi++) {
                    for (int yi = 0; yi < DIM_Y; yi++) {

                        final int _x = xi;
                        final int _y = yi;

                        Cell _cell = playground.getCell(_x, _y);

                        // Submit new life.process() call as runable to the pool
                        pool.submit(() -> life.process(_cell, _x, _y));
                        
                    }
                }
                try {
                    // Wait for all threads to finish this generation
                    pool.barrier();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Application.class.getName()).log(Level.SEVERE, "ZORTLADIN", ex);
                }
                // Submit switch to next generation for each cell and force a
                // window repaint to update the graphics
                window.validate();
                window.repaint();
                pool.submit(() -> {
                    playground.asList().forEach(cell -> cell.nextGen());
                    window.repaint();
                });

                try {
                    // Wait SLEEP milliseconds until the next generation
                    Thread.sleep(SLEEP);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }
}