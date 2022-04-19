package com.finalProject.OnlineGame.model;


import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Board {
    private int[][] grid = new int[3][3];
    private int XMark = 0; // 1
    private int OMark = 0; // 2
    private int available = 9;

    public void resetBoard() {
        grid = new int[3][3];
        available = 9;
        XMark = 0;
        OMark = 0;
    }

    public boolean checkAvailable() {
        if (available > 0) return true;
        else return false;
    }

    public void addMark(int row, int col, String type) throws Exception {
        available--;
        if (type.equals("cross")) {
            grid[row][col] = 1;
        } else if (type.equals("circle")) {
            grid[row][col] = 2;
        } else {
            throw new Exception("Illegal type");
        }
    }

    public int checkWin() {
        // check row
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i][j] == 1) XMark++;
                else if (grid[i][j] == 2) OMark++;
                else continue;
            }
            if (XMark == 3) {
                XMark = 0;
                OMark = 0;
                return 1;
            }
            else if (OMark == 3) {
                XMark = 0;
                OMark = 0;
                return 2;
            }
            else {
                XMark = 0;
                OMark = 0;
                continue;
            }
        }

        //check col
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[j][i] == 1) XMark++;
                else if (grid[j][i] == 2) OMark++;
                else continue;
            }
            if (XMark == 3) {
                XMark = 0;
                OMark = 0;
                return 1;
            }
            else if (OMark == 3) {
                XMark = 0;
                OMark = 0;
                return 2;
            }
            else {
                XMark = 0;
                OMark = 0;
                continue;
            }
        }

        // check top-left -> bottom-right
        int row = 0;
        int col = 0;
        while (row < 3 && col < 3) {
            if (grid[row][col] == 1) XMark++;
            else if (grid[row][col] == 2) OMark++;
            row++;
            col++;
        }
        if (XMark == 3) {
            XMark = 0;
            OMark = 0;
            return 1;
        }
        else if (OMark == 3) {
            XMark = 0;
            OMark = 0;
            return 2;
        }
        else {
            XMark = 0;
            OMark = 0;
        };


        //check top-right -> bottom-left
        row = 0;
        col = 2;
        while (row < 3 && col >= 0) {
            if (grid[row][col] == 1) XMark++;
            else if (grid[row][col] == 2) OMark++;
            row++;
            col--;
        }
        if (XMark == 3) {
            XMark = 0;
            OMark = 0;
            return 1;
        }
        else if (OMark == 3) {
            XMark = 0;
            OMark = 0;
            return 2;
        }
        else {
            XMark = 0;
            OMark = 0;
        };

        return -1;
    }
}
