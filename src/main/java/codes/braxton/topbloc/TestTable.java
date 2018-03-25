package codes.braxton.topbloc;

import com.google.gson.Gson;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.io.File;
import java.util.Arrays;

public class TestTable {

    private final double[] col1Nums;
    private final double[] col2Nums;
    private final String[] col3Strs;

    private TestTable(double[] col1Nums,
                      double[] col2Nums,
                      String[] col3Strs) {
        this.col1Nums = col1Nums;
        this.col2Nums = col2Nums;
        this.col3Strs = col3Strs;
    }

    public static TestTable createWith(double[] col1Nums,
                                       double[] col2Nums,
                                       String[] col3Strs)
            throws TestTableException {
        if (col1Nums.length == col2Nums.length
            && col2Nums.length == col3Strs.length) {
            return new TestTable(col1Nums, col2Nums, col3Strs);
        }

        throw TestTableException.paramLengthMismatch(col1Nums, col2Nums, col3Strs);
    }

    public static TestTable readFrom(String filePath)
            throws TestTableException {
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(new File(filePath));
        } catch (Exception e) {
            throw TestTableException.workbookReadExc(filePath, e);
        }

        if (workbook.getNumberOfSheets() < 1) {
            throw TestTableException.emptyWorkbookExc(workbook);
        }

        Sheet firstSheet = workbook.getSheetAt(0);

        TestTable table = readFrom(firstSheet);

        try { workbook.close(); }
        catch (java.io.IOException e) {} // this only throws for writeable packages

        return table;
    }

    public static TestTable readFrom(Sheet sheet)
            throws TestTableException {
        int firstRowNum = sheet.getFirstRowNum() + 1; // ignore header row
        int lastRowNum = sheet.getLastRowNum();

        return readFrom(sheet, firstRowNum, lastRowNum);
    }

    public static TestTable readFrom(Sheet sheet, int firstRowNum, int lastRowNum)
            throws TestTableException {
        int numRows = Math.max(lastRowNum - firstRowNum + 1, 0);

        double[] col1Nums = new double[numRows];
        double[] col2Nums = new double[numRows];
        String[] col3Strs = new String[numRows];

        for (int i = 0; i < numRows; ++i) {
            int rowNum = firstRowNum + i;
            Row row = sheet.getRow(rowNum);

            if (row == null) {
                throw TestTableException.missingRowExc(sheet, rowNum);
            }

            col1Nums[i] = getNumericCellValue(row, 0);
            col2Nums[i] = getNumericCellValue(row, 1);
            col3Strs[i] = getStringCellValue(row, 2);
        }

        return new TestTable(col1Nums, col2Nums, col3Strs);
    }

    private static double getNumericCellValue(Row row, int cellNum)
            throws TestTableException {
        Cell cell = row.getCell(cellNum);

        if (cell == null) {
            throw TestTableException.missingCellExc(row, cellNum);
        }

        double numVal;
        try {
            numVal = cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            throw TestTableException.cellValueExc(cell, CellType.NUMERIC);
        } catch (NumberFormatException e) {
            throw TestTableException.cellValueExc(cell, CellType.NUMERIC);
        }

        return numVal;
    }

    private static String getStringCellValue(Row row, int cellNum)
            throws TestTableException {
        Cell cell = row.getCell(cellNum);

        if (cell == null) {
            throw TestTableException.missingCellExc(row, cellNum);
        }

        String strVal;
        try {
            strVal = cell.getStringCellValue();
        } catch (IllegalStateException e) {
            throw TestTableException.cellValueExc(cell, CellType.STRING);
        } catch (NumberFormatException e) {
            throw TestTableException.cellValueExc(cell, CellType.STRING);
        }

        return strVal;
    }

    public void printValues() {
        for (int i = 0; i < col1Nums.length; ++i) {
            System.out.println(String.valueOf(col1Nums[i])
                               + "\t" + String.valueOf(col2Nums[i])
                               + "\t" + col3Strs[i]);
        }
    }

    public int getNumRows() {
        return col1Nums.length;
    }

    public double[] getCol1Nums() {
        return Arrays.copyOf(col1Nums, col1Nums.length);
    }

    public double[] getCol2Nums() {
        return Arrays.copyOf(col2Nums, col2Nums.length);
    }

    public String[] getCol3Strs() {
        return Arrays.copyOf(col3Strs, col3Strs.length);
    }

    public static TestTable combine(TestTable t1, TestTable t2)
            throws TestTableException {
        if (t1.getNumRows() != t2.getNumRows()) {
            throw TestTableException.tableLengthMismatch(t1, t2);
        }

        int numRows = t1.getNumRows();
        double[] col1Nums = new double[numRows];
        double[] col2Nums = new double[numRows];
        String[] col3Strs = new String[numRows];

        for (int i = 0; i < numRows; ++i) {
            col1Nums[i] = t1.col1Nums[i] * t2.col1Nums[i];
            col2Nums[i] = t1.col2Nums[i] / t2.col2Nums[i];
            col3Strs[i] = t1.col3Strs[i] + " " + t2.col3Strs[i];
        }

        return new TestTable(col1Nums, col2Nums, col3Strs);
    }

    private static class RequestContent {

        public final String id;
        public final double[] numberSetOne;
        public final double[] numberSetTwo;
        public final String[] wordSetOne;

        public RequestContent(String id,
                              double[] numberSetOne,
                              double[] numberSetTwo,
                              String[] wordSetOne) {
            this.id           = id;
            this.numberSetOne = numberSetOne;
            this.numberSetTwo = numberSetTwo;
            this.wordSetOne   = wordSetOne;
        }

    }

    public String createRequestJson(String email) {
        RequestContent req = new RequestContent(email, col1Nums, col2Nums, col3Strs);
        return new Gson().toJson(req);
    }

}
