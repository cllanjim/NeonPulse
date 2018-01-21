package extra;

import engine.Agent;
import engine.Level;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class GridAgent extends Agent {
    LooseGrid.Cell owner_cell;
    int cell_array_index;

    @Override
    public void update(Level level, float delta_time) {

    }

    @Override
    public void display(PGraphics graphics) {

    }

    public static class LooseGrid {
        List<Cell> cells;
        private float cell_size;
        private float width;
        private float height;
        int columns;
        int rows;

        LooseGrid(float width, float height, float cell_size, int reserve) {
            this.cell_size = cell_size;
            this.width = width;
            this.height = height;
            this.columns = PApplet.ceil(width/cell_size);
            this.rows = PApplet.ceil(height/cell_size);
            this.cells = new ArrayList<>(columns * rows);

            for (Cell cell: cells) {
                cell.agents.ensureCapacity(reserve);
            }
        }

        void addToCell(GridAgent agent) {
            Cell cell = getCellAt(agent.position);
            addToCell(agent, cell);
        }

        void addToCell(GridAgent agent, Cell cell) {
            cell.agents.add(agent);
            agent.owner_cell = cell;
            agent.cell_array_index = cell.agents.size() - 1;
        }

        void removeFromCell(GridAgent agent) {
            ArrayList<GridAgent> agents =  agent.owner_cell.agents;
            agents.remove(agent.cell_array_index);
            agent.cell_array_index = -1;
            agent.owner_cell = null;
        }

        ArrayList<GridAgent> getAgentsAt(int x, int y) {
            Cell cell = getCell(x, y);
            return cell.agents;
        }

        Cell getCellAt(PVector position) {
            int x = PApplet.floor(position.x / cell_size);
            int y = PApplet.floor(position.y / cell_size);
            return getCell(x, y);
        }

        private Cell getCell(int x, int y) {
            int col = (x < 0) ? 0 : (x >= columns) ? columns - 1 : x;
            int row = (y < 0) ? 0 : (y >= rows) ? rows - 1 : y;
            return cells.get(row * columns + col);
        }

        static class Cell {
            ArrayList<GridAgent> agents;
            Cell() {
                agents = new ArrayList<>();
            }
        }
    }

    // TODO: Grid where agents keep a list of cells they are in
    public static class TightGrid {

    }

    static class AgentController {
        private ArrayList<GridAgent> agents;
        private LooseGrid grid;
        private float width;
        private float height;

        AgentController(int width, int height) {
            this.agents = new ArrayList<>();
            this.grid = new LooseGrid(width, height, 16, 2);
            this.width = width;
            this.height = height;
        }

        void add(GridAgent agent) {
            agents.add(agent);
            grid.addToCell(agent);
        }

        void remove(GridAgent agent) {
            grid.removeFromCell(agent);
            agents.remove(agent);
        }

        void display(PGraphics g) {
            for (GridAgent agent: agents) {
                agent.display(g);
            }
        }

        void update(float delta_time){
            updateCollisions();
            updatePartitioning();
        }

        private void updatePartitioning() {
            for (GridAgent agent : agents) {
                // Check to see if the agent moved
                if (agent.owner_cell != null) {
                    LooseGrid.Cell newCell = grid.getCellAt(agent.position);
                    if (newCell != agent.owner_cell) {
                        grid.removeFromCell(agent);
                        grid.addToCell(agent, newCell);
                    }
                }
            }
        }

        private void updateCollisions() {
            for (int i = 0; i < grid.cells.size(); i++) {
                LooseGrid.Cell cell = grid.cells.get(i);
                int x = i % grid.columns;
                int y = i / grid.columns;

                for (int j = 0; j < cell.agents.size(); j++) {
                    Agent agent = cell.agents.get(i);
                    checkCollisions(agent, cell.agents, j + 1);
                    // Neighbor cells: left, top left, bottom left and up
                    if ( x > 0 ) {
                        checkCollisions(agent, grid.getAgentsAt(x - 1, y), 0);
                        if ( y > 0 ) checkCollisions(agent, grid.getAgentsAt(x - 1, y - 1), 0);
                        if ( y < grid.rows - 1 ) checkCollisions(agent, grid.getAgentsAt(x - 1, y + 1), 0);
                    }
                    if ( y > 0 ) checkCollisions(agent, grid.getAgentsAt(x, y - 1), 0);
                }
            }
        }

        private static void checkCollisions(Agent agent, ArrayList<GridAgent> agent_list, int first_index) {
            for (int i = first_index; i < agent_list.size(); i++) {
                Agent other = agent_list.get(i);
                checkCollision(agent, other);
            }
        }

        private static void checkCollision(Agent agent, Agent other) {
            PVector dist_vec = PVector.sub(other.position, agent.position);
            float dist = dist_vec.mag();
            float min_distance = agent.radius + other.radius;
            float collision_depth = min_distance - dist;

            if ( collision_depth > 0 ) {
                // Get direction of distance vector
                dist_vec.normalize();

                // Move the less massive one
                if ( agent.mass < other.mass ) {
                    agent.position.add(PVector.mult(dist_vec, -collision_depth));
                } else {
                    other.position.add(PVector.mult(dist_vec, collision_depth));
                }

                // Calculate energy/momentum transfer
                float aci = PVector.dot(agent.velocity, dist_vec);
                float bci = PVector.dot(other.velocity, dist_vec);
                float acf = ( aci * ( agent.mass - other.mass ) + 2 * other.mass * bci ) / ( agent.mass + other.mass );
                float bcf = ( bci * ( other.mass - agent.mass ) + 2 * agent.mass * aci ) / ( agent.mass + other.mass );

                // Alter movement direction
                agent.velocity.add(PVector.mult(dist_vec, acf - aci ));
                other.velocity.add(PVector.mult(dist_vec, bcf - bci ));
            }
        }

    }
}
