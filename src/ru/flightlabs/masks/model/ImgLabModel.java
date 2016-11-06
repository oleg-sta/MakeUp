package ru.flightlabs.masks.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.flightlabs.masks.model.primitives.Line;
import ru.flightlabs.masks.model.primitives.Point;

public class ImgLabModel implements SimpleModel {

    Point[] points;
    Line[] lines;

    public ImgLabModel(String fileModel) {
        List<Point> points = new ArrayList<Point>(1000);
        BufferedReader br;
        try {
            // TODO ������ xml, �� ��� ���� ��� ������������, ����� ��� ��������� ������
            // ������������, ��� ��� ���� �����������
            br = new BufferedReader(new FileReader(fileModel));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (line.contains("part name")) {
                    points.add(Integer.parseInt(getAttribute(line, "name")), new Point(Double.parseDouble(getAttribute(line, "x")), Double.parseDouble(getAttribute(line, "y")), Integer.parseInt(getAttribute(line, "name"))));
                    i++;
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.points = points.toArray(new Point[0]);
        //Utils.upsideDown(this.points);
        int[] liness = {17,5,5,4,5,6,6,12,8};
        boolean[] closedLiness = {false, false, false, false, false, true, true, true, true};
        List<Line> lines = new ArrayList<Line>();
        int lineCount = 0;
        int j = 0;
        for (int lineLength : liness) {
            int startPoint = j;
            int endPoint = j;
            for (int i = 1; i < lineLength; i++) {
                endPoint = j + 1;
                if (j + 1 < points.size()) {
                    lines.add(new Line(j, j + 1, true));
                }
                j++;
            }
            if (closedLiness[lineCount]) {
                lines.add(new Line(startPoint, endPoint, true));
            }
            j++;
            lineCount++;
        }
        this.lines = lines.toArray(new Line[0]);
    }

    private String getAttribute(String line, String string) {
        int indexOf = line.indexOf(string + "='");
        return line.substring(indexOf + string.length() + 2, line.indexOf("'", indexOf + string.length() + 2));
    }

    @Override
    public Point[] getPointsWas() {
        return points;
    }

    @Override
    public Point[] getPointsTo() {
        return points.clone();
    }

    @Override
    public Line[] getLines() {
        return lines;
    }


}
