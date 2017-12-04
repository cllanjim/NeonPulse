package engine;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

class Grid {
    ArrayList<Cell> cells;
    private float cell_size;
    private float width;
    private float height;
    int columns;
    int rows;

    Grid(float width, float height, float cell_size, int reserve) {
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

    void addToCell(Agent agent) {
        Cell cell = getCellAt(agent.position);
        addToCell(agent, cell);
    }

    void addToCell(Agent agent, Cell cell) {
        cell.agents.add(agent);
        agent.owner_cell = cell;
        agent.cell_array_index = cell.agents.size() - 1;
    }

    void removeFromCell(Agent agent) {
        ArrayList<Agent> agents =  agent.owner_cell.agents;
        agents.remove(agent.cell_array_index);
        agent.cell_array_index = -1;
        agent.owner_cell = null;
    }

    ArrayList<Agent> getAgentsAt(int x, int y) {
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
        ArrayList<Agent> agents;
        Cell() {
            agents = new ArrayList<>();
        }
    }
}
