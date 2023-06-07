/*******************************************************************************
 * Copyright 2016 SPECURE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.alladin.rmbt.statisticServer.report;

import at.alladin.rmbt.shared.db.Test;
import at.alladin.rmbt.shared.db.fields.Field;
import at.alladin.rmbt.shared.db.repository.FieldAccess;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class TestReportUtil {

    // logger instance
    private static final Logger logger = LoggerFactory.getLogger(TestReportUtil.class);


    public static class TestReportCategory {
        final Point position;
        final String titleKey;
        final List<TestReportField<? extends FieldAccess>> fieldList;

        @SafeVarargs
        public TestReportCategory(final String titleKey, TestReportField<? extends FieldAccess>... fields) {
            this(titleKey, Arrays.asList(fields));
        }

        @SafeVarargs
        public TestReportCategory(final Point position, final String titleKey, TestReportField<? extends FieldAccess>... fields) {
            this(position, titleKey, Arrays.asList(fields));
        }

        public TestReportCategory(final String titleKey, final List<TestReportField<? extends FieldAccess>> fieldList) {
            this(new Point(0, 0), titleKey, fieldList);
        }

        public TestReportCategory(final Point position, final String titleKey, final List<TestReportField<? extends FieldAccess>> fieldList) {
            this.position = position;
            this.titleKey = titleKey;
            this.fieldList = fieldList;
        }

        public String getTitleKey() {
            return titleKey;
        }

        public List<TestReportField<? extends FieldAccess>> getFieldList() {
            return fieldList;
        }

        public Point getPosition() {
            return position;
        }
    }

    public static class TestReportField<O extends FieldAccess> {
        final String labelKey;
        final ReportCell<O> value;

        public TestReportField(final String labelKey, final String valueKey) {
            this.labelKey = labelKey;
            this.value = new StringReportCell<O>(valueKey, null);
        }

        public TestReportField(final String labelKey, final ReportCell<O> value) {
            this.labelKey = labelKey;
            this.value = value;
        }

        public String getLabelKey() {
            return labelKey;
        }

        public ReportCell<O> getValue() {
            return value;
        }
    }

    public static interface CellFormatter {
        void formatCell(CreationHelper helper, Cell cell, String value, Field field);
    }

    public static class HyperlinkCellFormat implements CellFormatter {

        public static interface HyperlinkGenerator {
            String generate(final String value, final Field field);
        }

        final HyperlinkGenerator hyperlinkGenerator;

        public HyperlinkCellFormat(final HyperlinkGenerator hyperlinkGenerator) {
            this.hyperlinkGenerator = hyperlinkGenerator;
        }

        @Override
        public void formatCell(final CreationHelper helper, final Cell cell, final String value, final Field field) {
            try {
                final String url = hyperlinkGenerator.generate(value, field);
                if (url != null) {
                    final Hyperlink link = helper.createHyperlink(XSSFHyperlink.LINK_URL);
                    link.setAddress(url);
                    cell.setHyperlink(link);
                }
            } catch (final Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    public static interface ValueFormatter {
        Object format(final Field value);
    }

    public static abstract class ReportCell<O extends FieldAccess> {
        final String key;
        CellFormatter cellFormat = null;

        public ReportCell(final String key, final CellFormatter cellFormat) {
            this.key = key;
            this.cellFormat = cellFormat;
        }

        protected abstract String getString(final O value);

        public String getValue(final CreationHelper helper, final Cell cell, final O value) {
            final String result = getString(value);
            if (cellFormat != null) {
                cellFormat.formatCell(helper, cell, result, value.getField(key));
            }
            return result;
        }
    }

    public static class StringReportCell<O extends FieldAccess> extends ReportCell<O> {
        final ValueFormatter valueFormatter;

        public StringReportCell(final String key, final CellFormatter cellFormat) {
            this(key, cellFormat, null);
        }

        public StringReportCell(final String key, final CellFormatter cellFormat, final ValueFormatter valueFormatter) {
            super(key, cellFormat);
            this.valueFormatter = valueFormatter;
        }

        @Override
        protected String getString(O value) {
            return valueFormatter == null ? value.getField(key).toString() : String.valueOf(valueFormatter.format(value.getField(key)));
        }
    }

    public static class FormattedReportCell<O extends FieldAccess> extends ReportCell<O> {
        final String format;
        final String label;
        final ValueFormatter valueFormatter;

        public FormattedReportCell(final String key, final String format, final String label, final CellFormatter cellFormat) {
            this(key, format, label, cellFormat, null);
        }

        public FormattedReportCell(final String key, final String format, final String label, final CellFormatter cellFormat, ValueFormatter formatCallback) {
            super(key, cellFormat);
            this.format = format;
            this.label = label;
            this.valueFormatter = formatCallback;
        }

        @Override
        protected String getString(O value) {
            return String.format(format, valueFormatter == null ? value : valueFormatter.format(value.getField(key)), label);
        }
    }

    public static XSSFWorkbook getTestReport(final Test test, final List<TestReportCategory> reportList, final ResourceBundle labels, final String lang) {
        final XSSFWorkbook wb = new XSSFWorkbook();
        final XSSFCellStyle boldStyle = wb.createCellStyle();
        final CreationHelper helper = wb.getCreationHelper();
        boldStyle.setBorderColor(BorderSide.BOTTOM, new XSSFColor(new Color(0, 0, 128)));
        boldStyle.setBorderBottom(BorderStyle.THICK);
        final Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        boldStyle.setFont(font);

        final XSSFSheet sheet = wb.createSheet();
        sheet.setColumnWidth(1, 8800);
        sheet.setColumnWidth(2, 8500);

        final XSSFCellStyle dataCellStyle = wb.createCellStyle();
        dataCellStyle.setBorderColor(BorderSide.BOTTOM, new XSSFColor(new Color(178, 178, 178)));
        dataCellStyle.setBorderBottom(BorderStyle.MEDIUM);
        final Font dataCellFont = wb.createFont();
        dataCellFont.setFontHeightInPoints((short) 11);
        dataCellStyle.setFont(dataCellFont);

        for (final TestReportCategory cat : reportList) {
            int posX = cat.getPosition().x;
            int posY = cat.getPosition().y;

            final Row row = sheet.createRow(posY);
            final Cell c = row.createCell(posX);
            c.setCellValue(labels.getString(cat.getTitleKey()));
            c.setCellStyle(boldStyle);
            sheet.addMergedRegion(new CellRangeAddress(posY, posY++, posX, posX + 1));

            for (int y = 0; y < cat.fieldList.size(); y++) {
                @SuppressWarnings("unchecked") final TestReportField<Test> field = (TestReportField<Test>) cat.fieldList.get(y);

                addCell(sheet, posY + y, posX, labels.getString(field.getLabelKey()), dataCellStyle);
                addCell(sheet, posY + y, posX + 1, helper, field.getValue(), test, dataCellStyle);
            }
        }

        return wb;
    }

    private static void addCell(Sheet sheet, int row, int cell, String value, CellStyle style) {
        Row dataRow = sheet.getRow(row);
        if (dataRow == null) {
            dataRow = sheet.createRow(row);
        }
        final Cell labelCell = dataRow.createCell(cell);
        labelCell.setCellValue(value);
        labelCell.setCellStyle(style);
    }

    private static void addCell(Sheet sheet, int row, int cell, CreationHelper helper, ReportCell<Test> value, Test test, CellStyle style) {
        Row dataRow = sheet.getRow(row);
        if (dataRow == null) {
            dataRow = sheet.createRow(row);
        }
        final Cell labelCell = dataRow.createCell(cell);
        labelCell.setCellValue(value.getValue(helper, labelCell, test));
        labelCell.setCellStyle(style);
    }
}
