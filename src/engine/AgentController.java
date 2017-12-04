package engine;

import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

class AgentController {
    private ArrayList<Agent> agents;
    private Grid grid;
    private float width;
    private float height;

    AgentController(int width, int height) {
        this.agents = new ArrayList<>();
        this.grid = new Grid(width, height, 16, 2);
        this.width = width;
        this.height = height;
    }

    void add(Agent agent) {
        agents.add(agent);
        grid.addToCell(agent);
    }

    void remove(Agent agent) {
        grid.removeFromCell(agent);
        agents.remove(agent);
    }

    void display(PGraphics g) {
        for (Agent agent: agents) {
            agent.display(g);
        }
    }

    void update(float delta_time){
        updateCollisions();
        updatePartitioning();
    }

    private void updatePartitioning() {
        for (Agent agent : agents) {
            // Check to see if the agent moved
            if (agent.owner_cell != null) {
                Grid.Cell newCell = grid.getCellAt(agent.position);
                if (newCell != agent.owner_cell) {
                    grid.removeFromCell(agent);
                    grid.addToCell(agent, newCell);
                }
            }
        }
    }

    private void updateCollisions() {
        for (int i = 0; i < grid.cells.size(); i++) {
            Grid.Cell cell = grid.cells.get(i);
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

    private void checkCollisions(Agent agent, ArrayList<Agent> agent_list, int first_index) {
        for (int i = first_index; i < agent_list.size(); i++) {
            Agent other = agent_list.get(i);
            checkCollision(agent, other);
        }
    }

    private void checkCollision(Agent agent, Agent other) {
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