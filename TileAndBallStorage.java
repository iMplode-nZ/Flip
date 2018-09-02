import javafx.scene.canvas.GraphicsContext;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

class TileAndBallStorage {
    private ArrayList<ArrayList<Tile>> tiles;
    private ArrayList<Ball> balls;
    private int tileSize;
    TileAndBallStorage(int sizeTile) {
        tiles = new ArrayList<>();
        balls = new ArrayList<>();
        tileSize = sizeTile;
    }


    void read(File f) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            tiles = new ArrayList<>();
            int a = 0;
            boolean twoNewLines = false;
            while(true) {
                ArrayList<Tile> al = new ArrayList<>();
                int b = 0;
                while(true) {
                    int c = br.read();
                    if(c != '\n') {
                        al.add(Tile.create(br.read(), tileSize));
                        twoNewLines = false;
                    } else {
                        tiles.add(al);
                        break;
                    }
                }
                if(twoNewLines) {
                    break;
                }
                twoNewLines = true;
                a++;
            }
            while(true) {
                if(br.read()=='[') {
                    break;
                }
            }
            Scanner sc = new Scanner(br);
            if(br.skip(1)!=1) throw new IOException("Warning: skip(long n) is not skipping n.");
            do {
                sc.useDelimiter(",");
                int x = sc.nextInt();
                int y = sc.nextInt();
                int number = sc.nextInt();
                sc.useDelimiter("}");
                Direction d = Direction.getString(sc.next());
                if(br.skip(1)!=1) throw new IOException("Warning: skip(long n) is not skipping n."); 
                balls.add(new Ball(d, x, y, tileSize, number));
            } while (sc.hasNext());
            sc.close();
        } catch(IOException e) {
            System.err.println("Warning: Could not read from file.");
            e.printStackTrace();
        }
        removeEmpty();
    }
    void write(File f) {
        removeEmpty();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for(int a = 0; a < tiles.size(); a++) {
                for(int b = 0; b < tiles.size(); b++) {
                    bw.write(tiles.get(a).get(b).getAscii());
                }
                bw.newLine();
            }
            bw.newLine();
            bw.write("Balls:[");
            for (Ball b : balls) {
                bw.write("{" + b.x + "," + b.y + "," + b.number + "," + b.thisDirection.toString() + "}");
            }
            bw.write("]");
            bw.close();
        } catch(IOException e) {
            System.err.println("Warning: Could not write to file.");
            e.printStackTrace();
        }
    }
    private void removeEmpty() {
        for (ArrayList<Tile> tile : tiles) {
            while (true) {
                if (tile.get(tile.size() - 1) instanceof Empty) {
                    tile.remove(tile.size() - 1);
                } else {
                    break;
                }
            }
        }
        while(true) {
            if(tiles.get(tiles.size()-1).size()==0) {
                tiles.remove(tiles.size()-1);
            } else {
                break;
            }
        }
    }
    Tile getTileAtPos(int x, int y) {
        if(tiles.size()>x) {
            if(tiles.get(x).size()>y) {
                return tiles.get(x).get(y);
            }
        }
        return new Empty(Direction.NORTHSOUTHEASTWEST,tileSize);
    }
    void draw(GraphicsContext gc) {
        for (int a = 0; a < tiles.size(); a++) {
            for (int b = 0; b < tiles.size(); b++) {
                tiles.get(a).get(b).draw(gc, a, b);
            }
        }
        for (Ball ball : balls) {
            ball.draw(gc);
        }
    }
    synchronized void update() {
        for (Ball ball : balls) {
            ball.update(this);
        }
    }
    void place(GraphicsObject go, int x, int y) {
        if(go instanceof Ball) {
            placeBall((Ball) go);
        }
        if(go instanceof Tile) {
            placeTile((Tile) go, x, y);
        }
    }
    private void placeBall(Ball b) {
        balls.add(b);
    }
    private void placeTile(Tile t, int x, int y) {
        if(tiles.size()>x) {
            if(tiles.get(x).size()>y) {
                tiles.get(x).set(y,t);
            } else {
                for(int i = 0; i < y - tiles.get(x).size() + 1; i++) {
                    tiles.get(x).add(new Empty(Direction.NORTHSOUTHEASTWEST,tileSize));
                }
                placeTile(t,x,y);
            }
        } else {
            for(int i = 0; i < x - tiles.size() + 1; i++) {
                tiles.add(new ArrayList<>());
            }
            placeTile(t,x,y);
        }
    }
    void remove(int x, int y) {
        removeBall(x,y);
        removeTile(x,y);
        removeEmpty();
    }
    void removeBall(int x, int y) {
        balls.removeIf(ball -> ball.x == x && ball.y == y);
    }
    private void removeTile(int x, int y) {
        if(x < tiles.size()) {
            if(y < tiles.get(x).size()) {
                tiles.get(x).set(y, new Empty(Direction.NORTHSOUTHEASTWEST, tileSize));
            }
        }
    }
    void removeRect(int startX, int startY, int endX, int endY) {
        for(int a = startX; a < endX; a++) {
            for(int b = startY; b < endY; b++) {
                remove(a,b);
            }
        }
    }
    void removeCol(int whichCol) {
        tiles.remove(whichCol);
    }
    void removeRow(int whichRow) {
        for (ArrayList<Tile> tile : tiles) {
            tile.remove(whichRow);
        }
    }
    void deleteRect(int startX, int startY, int endX, int endY) {
        int a = startX;
        do {
            int b = startY;
            do {
                tiles.get(a).remove(startY);
                b++;
            } while (b != endY);
            a++;
        } while (a != endX);
    }
}
