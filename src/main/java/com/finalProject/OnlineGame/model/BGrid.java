package com.finalProject.OnlineGame.model;

import lombok.Data;

@Data
public class BGrid {

    private BLocation[][] grid;
    private int shipNumLeft;

    public BGrid() {
        grid = new BLocation[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                grid[i][j] = new BLocation();
            }
        }
        this.shipNumLeft = 5;
    }

    public boolean shoot(int row, int col) {
        if (row >= 10 || col >= 10 || row < 0 || col < 0) {
            return false;
        }

        if (grid[row][col].getHasShip() && grid[row][col].getStatus() == BLocation.UNGUESSED) {
            grid[row][col].setStatus(BLocation.HIT);
            return true;
        } else {
            return false;
        }
    }

    public boolean addShip(int row, int col, int length, String direction, int shipHeadRow, int shipHeadCol) {
        if (!grid[row][col].getHasShip()) {
            grid[row][col].setHasShip(true);
            grid[row][col].setLen(length);
            grid[row][col].setDir(direction);
            grid[row][col].setShipHeadRow(shipHeadRow);
            grid[row][col].setShipHeadCol(shipHeadCol);
            return true;
        }
        else return false;
    }

    public boolean checkDead(int row, int col) {
        if (grid[row][col].getStatus() != BLocation.HIT) return false;

        int len = grid[row][col].getLen();
        String dir = grid[row][col].getDir();
        int shipHeadRow = grid[row][col].getShipHeadRow();
        int shipHeadCol = grid[row][col].getShipHeadCol();
        if (dir.equals("hor")) {
            for (int i = shipHeadCol; i < shipHeadCol + len; i++) {
                if (grid[row][i].getStatus() == BLocation.UNGUESSED) {
                    return false;
                }
            }

            for (int i = shipHeadCol; i < shipHeadCol + len; i++) {
                grid[row][i].setStatus(BLocation.DEAD);
            }
            shipNumLeft--;
            return true;

        } else {
            for (int i = shipHeadRow; i < shipHeadRow + len; i++) {
                if (grid[i][col].getStatus() == BLocation.UNGUESSED) {
                    return false;
                }
            }

            for (int i = shipHeadRow; i < shipHeadRow + len; i++) {
                grid[i][col].setStatus(BLocation.DEAD);
            }
            shipNumLeft--;
            return true;
        }
    }

    public boolean checkWin() {
        if (this.shipNumLeft == 0) return true;
        else return false;
    }

}
