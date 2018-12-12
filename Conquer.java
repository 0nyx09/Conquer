import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.sql.*;
import java.util.*;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import com.google.common.base.Joiner;

import com.google.common.collect.Sets;

class Conquer{
    public static String qStarPhi = "SELECT * FROM ";
    public static String qStar = "";
    public static ArrayList<String> selectionAttributes = new ArrayList<>();
    public static ArrayList<String> selectionAttributeOperators = new ArrayList<>();
    public static ArrayList<ArrayList<String>> selectionAttributeValues = new ArrayList<>();
    public static HashMap<String, String> attributeTypes = new HashMap<>();

    public static int d = 0;

    static class ReplaceLongValues extends ExpressionDeParser {
        @Override
        public void visit(AndExpression expr) {
            ArrayList<String> values = selectionAttributeValues.get(1);
            if (expr.getLeftExpression() instanceof AndExpression) {
                expr.getLeftExpression().accept(this);
            }
            if ((expr.getLeftExpression() instanceof MinorThanEquals)){
                ((MinorThanEquals) expr.getLeftExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
            if ((expr.getRightExpression() instanceof MinorThanEquals)){
                ((MinorThanEquals) expr.getRightExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
            if ((expr.getLeftExpression() instanceof MinorThan)){
                ((MinorThan) expr.getLeftExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
            if ((expr.getRightExpression() instanceof MinorThan)){
                ((MinorThan) expr.getRightExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
            if ((expr.getLeftExpression() instanceof GreaterThanEquals)){
                ((GreaterThanEquals) expr.getLeftExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
            if ((expr.getRightExpression() instanceof GreaterThanEquals)){
                ((GreaterThanEquals) expr.getRightExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
            if ((expr.getLeftExpression() instanceof GreaterThan)){
                ((GreaterThan) expr.getLeftExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
            if ((expr.getRightExpression() instanceof GreaterThan)){
                ((GreaterThan) expr.getRightExpression()).setRightExpression(new LongValue(values.get(0)));
                values.remove(0);
                if(values.isEmpty()) {
                    this.getBuffer().append(expr);
                }
            }
        }
    }

    public static String cleanStatement(String sql) throws JSQLParserException {
        StringBuilder buffer = new StringBuilder();
        ExpressionDeParser expr = new Conquer.ReplaceLongValues();
        d += selectionAttributeValues.get(1).size();

        SelectDeParser selectDeparser = new SelectDeParser(expr, buffer);
        expr.setSelectVisitor(selectDeparser);
        expr.setBuffer(buffer);
        StatementDeParser stmtDeparser = new StatementDeParser(expr, selectDeparser, buffer);

        net.sf.jsqlparser.statement.Statement stmt = CCJSqlParserUtil.parse(sql);

        stmt.accept(stmtDeparser);
        return stmtDeparser.getBuffer().toString();
    }

    public static String getRefinedQuery(String query) throws JSQLParserException {
        String refinedQuery = cleanStatement(query);
        selectionAttributeValues.remove(1);
        return refinedQuery;
    }

    public static ArrayList<ArrayList<String>> addToM(ArrayList<ArrayList<String>> m, ArrayList<ArrayList<String>> allTuples,
                                                      ArrayList<String> s) {
        String firstAttr = s.get(0);
        String firstVal = s.get(1);
        for (int j = 0; j < allTuples.get(0).size(); j++) {
            if (allTuples.get(0).get(j).equals(firstAttr)) {
                for (int i = 1; i < allTuples.size(); i++) {
                    if (allTuples.get(i).get(j).equals(firstVal)) {
                        if(s.size() > 2) {
                            boolean flag = true;
                            for(int attrInd = 2; attrInd < s.size(); attrInd += 2) {
                                String currentAttr = s.get(attrInd);
                                String currentVal = s.get(attrInd + 1);
                                for (int l = 0; l < allTuples.get(0).size(); l++) {
                                    if (allTuples.get(0).get(j).equals(currentAttr)) {
                                        if (allTuples.get(i).get(j).equals(currentVal)) {
                                            flag = false;
                                        }
                                    }
                                }
                            }
                            if(flag) {
                                m.add(allTuples.get(i));
                            }
                        }
                        else {
                            m.add(allTuples.get(i));
                        }
                    }
                }
            }
        }

        return m;
    }

    public static ArrayList<String> getSkylineTuples(ArrayList<String> s, ArrayList<ArrayList<String>> allTuples) throws Exception {

        ArrayList<ArrayList<String>> m = new ArrayList<>();
        m.add(allTuples.get(0));
        m = addToM(m, allTuples, s);

        selectionAttributeValues.add(selectionAttributes);
        for(int i = 1; i < m.size(); i++) {
            ArrayList<String> row = new ArrayList<>();
            for(int j = 0; j < m.get(0).size(); j++) {
                if(selectionAttributes.contains(m.get(0).get(j))) {
                    row.add(m.get(i).get(j));
                }
            }
            selectionAttributeValues.add(row);
        }

        ArrayList<String> skylineTuples = new ArrayList<>();
        for(int j = 0; j < selectionAttributeValues.get(0).size(); j++) {
            if (selectionAttributeOperators.get(j).equals("<=") || selectionAttributeOperators.get(j).equals("<")) {
                for (int i = 1; i < selectionAttributeValues.size(); i++) {
                    boolean minorThanEqualsflag = true;
                    boolean minorThanflag = false;
                    for (int k = 1; k < selectionAttributeValues.size(); k++) {
                        if (i != k) {
                            try {
                                if (Double.parseDouble(selectionAttributeValues.get(i).get(j)) >
                                        Double.parseDouble(selectionAttributeValues.get(k).get(j))) {
                                    minorThanEqualsflag = false;
                                }
                                if (Double.parseDouble(selectionAttributeValues.get(i).get(j)) <
                                        Double.parseDouble(selectionAttributeValues.get(k).get(j))) {
                                    minorThanflag = true;
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    if (minorThanEqualsflag && minorThanflag) {
                        skylineTuples.add(m.get(i).get(m.get(0).size()));
                    }
                }
            } else {
                for (int i = 1; i < selectionAttributeValues.size(); i++) {
                    boolean majorThanEqualsflag = true;
                    boolean majorThanflag = false;
                    for (int k = 1; k < selectionAttributeValues.size(); k++) {
                        if (i != k) {
                            try {
                                if (Double.parseDouble(selectionAttributeValues.get(i).get(j)) <
                                        Double.parseDouble(selectionAttributeValues.get(k).get(j))) {
                                    majorThanEqualsflag = false;
                                }
                                if (Double.parseDouble(selectionAttributeValues.get(i).get(j)) >
                                        Double.parseDouble(selectionAttributeValues.get(k).get(j))) {
                                    majorThanflag = true;
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    if (majorThanEqualsflag && majorThanflag) {
                        skylineTuples.add(m.get(i).get(m.get(0).size()));
                    }
                }
            }
        }

        if(!skylineTuples.isEmpty()) {
            for (int i = 1; i < m.size(); i++) {
                if (!skylineTuples.contains(m.get(i).get(m.get(0).size()))) {
                    selectionAttributeValues.remove(i);
                    m.remove(i);
                }
            }
        }
        else {
            for (int i = 1; i < m.size(); i++) {
                skylineTuples.add(m.get(i).get(m.get(0).size()));
            }
        }

        return skylineTuples;
    }

    public static String addSelectionPredicates(String refinedQuery, ArrayList<ArrayList<String>> allTuples,
                                                ArrayList<ArrayList<String>> queryTuples, ArrayList<ArrayList<String>> s,
                                                ArrayList<String> select, int count, int idealCount, Statement stmt,
                                                Set<String> keys) throws SQLException {

        ArrayList<ArrayList<String>> m = new ArrayList<>();
        m.add(allTuples.get(0));
        for(ArrayList<String> whyNot: s) {
            m = addToM(m, allTuples, whyNot);
        }

        for(ArrayList<String> tuple: queryTuples) {
            if(!containsWhyNot(tuple, m)) {
                m = addToM(m, allTuples, tuple);
            }
        }

        ArrayList<ArrayList<String>> v = new ArrayList<>();
        v.add(m.get(0));
        ArrayList<String> maxRow = new ArrayList<>();
        ArrayList<String> minRow = new ArrayList<>();
        for (int j = 0; j < m.get(0).size(); j++) {
            double colMax = Double.MIN_VALUE;
            double colMin = Double.MAX_VALUE;
            for (int i = 1; i < m.size(); i++) {
                if (!(keys.contains(m.get(0).get(j)) || select.contains(m.get(0).get(j))
                        || selectionAttributes.contains(m.get(0).get(j)))) {
                    if(m.get(i).get(j) != null) {
                        try {
                            if (Double.parseDouble(m.get(i).get(j)) > colMax) {
                                colMax = Double.parseDouble(m.get(i).get(j));
                            }
                            if (Double.parseDouble(m.get(i).get(j)) < colMin) {
                                colMin = Double.parseDouble(m.get(i).get(j));
                            }
                        } catch (NumberFormatException ex) {
                            colMax = -1;
                            colMin = -1;
                        }
                    }
                }
                else {
                    colMax = -1;
                    colMin = -1;
                }
            }
            if(colMax == Double.MIN_VALUE) {
                colMax = -1;
            }
            if(colMin == Double.MAX_VALUE) {
                colMin = -1;
            }
            if(colMax % 1 == 0) {
                maxRow.add(Integer.toString((int)colMax));
            }
            else {
                maxRow.add(Double.toString(colMax));
            }
            if(colMin % 1 == 0) {
                minRow.add(Integer.toString((int)colMin));
            }
            else {
                minRow.add(Double.toString(colMin));
            }
        }
        v.add(maxRow);
        v.add(minRow);

        for (int j = 0; j < v.get(0).size(); j++) {
            if (!(v.get(1).get(j).equals("-1.0"))) {
                String newCol = v.get(0).get(j);
                String newValMax = v.get(1).get(j);
                String newValMin = v.get(2).get(j);
                String newRefinedQueryMax;
                String newRefinedQueryMin;
                if(refinedQuery.contains("WHERE")) {
                    newRefinedQueryMax = refinedQuery + " AND " + newCol + " <= " + newValMax;
                    newRefinedQueryMin = refinedQuery + " AND " + newCol + " >= " + newValMin;
                }
                else {
                    newRefinedQueryMax = refinedQuery + " WHERE " + newCol + " <= " + newValMax;
                    newRefinedQueryMin = refinedQuery + " WHERE " + newCol + " >= " + newValMin;
                }
                ResultSet newRefinedQueryMaxRS = stmt.executeQuery(newRefinedQueryMax);
                ArrayList<ArrayList<String>> refinedQueryMaxTuples = new ArrayList<>();
                while(newRefinedQueryMaxRS.next()) {
                    ArrayList<String> queryRow = new ArrayList<>();
                    for(String selAttr: select) {
                        if(selAttr.contains(".")) {
                            selAttr = selAttr.split("\\.")[1];
                        }
                        queryRow.add(selAttr);
                        queryRow.add(newRefinedQueryMaxRS.getString(selAttr.toString()));
                    }
                    if(!containsVal(queryRow, refinedQueryMaxTuples)) {
                        refinedQueryMaxTuples.add(queryRow);
                    }
                }
                ResultSet newRefinedQueryMinRS = stmt.executeQuery(newRefinedQueryMin);
                ArrayList<ArrayList<String>> refinedQueryMinTuples = new ArrayList<>();
                while(newRefinedQueryMinRS.next()) {
                    ArrayList<String> queryRow = new ArrayList<>();
                    for(String selAttr: select) {
                        queryRow.add(selAttr);
                        queryRow.add(newRefinedQueryMinRS.getString(selAttr));
                    }
                    if(!containsVal(queryRow, refinedQueryMinTuples)) {
                        refinedQueryMinTuples.add(queryRow);
                    }
                }
                int newMaxCount = refinedQueryMaxTuples.size();
                int newMinCount = refinedQueryMinTuples.size();
                boolean maxValidated = isValid(s, queryTuples, refinedQueryMaxTuples);
                boolean minValidated = isValid(s, queryTuples, refinedQueryMinTuples);
                if(newMaxCount < newMinCount) {
                    if(maxValidated) {
                        if (newMaxCount >= idealCount && newMaxCount < count) {
                            refinedQuery = newRefinedQueryMax;
                            count = newMaxCount;
                            selectionAttributes.add(newCol);
                            d += 3;
                        }

                        if (newMaxCount == idealCount) {
                            break;
                        }
                    }
                }
                else {
                    if(minValidated) {
                        if (newMinCount >= idealCount && newMinCount < count) {
                            refinedQuery = newRefinedQueryMin;
                            count = newMinCount;
                            selectionAttributes.add(newCol);
                            d += 3;
                        }

                        if (newMinCount == idealCount) {
                            break;
                        }
                    }
                }
            }
        }

        return refinedQuery;
    }

    public static ArrayList<String> computeRefinedQueries(Statement stmt, String query, ArrayList<ArrayList<String>> s,
                                                          ArrayList<ArrayList<String>> allTuples, ArrayList<ArrayList<String>> queryTuples,
                                                          ArrayList<String> select, Set<String> keys) throws Exception {
        ArrayList<ArrayList<String>> v = new ArrayList<>();
        v.add(selectionAttributes);
        ResultSet qStarRS = stmt.executeQuery(qStar);
        ArrayList<String> minMaxRow = new ArrayList<>();
        for(int i = 0; i < selectionAttributes.size(); i++) {
            if (selectionAttributeOperators.get(i).equals("<=") || selectionAttributeOperators.get(i).equals("<")) {
                double max = Double.MIN_VALUE;
                while (qStarRS.next()) {
                    String val = qStarRS.getString(selectionAttributes.get(i));
                    if (val != null) {
                        if (Double.parseDouble(val) > max) {
                            max = Double.parseDouble(val);
                        }
                    }
                }
                minMaxRow.add(Double.toString(max));
            }
            else {
                double min = Double.MAX_VALUE;
                while (qStarRS.next()) {
                    String val = qStarRS.getString(selectionAttributes.get(i));
                    if (val != null) {
                        if (Double.parseDouble(val) < min) {
                            min = Double.parseDouble(val);
                        }
                    }
                }
                minMaxRow.add(Double.toString(min));
            }
            qStarRS.beforeFirst();
        }
        v.add(minMaxRow);

        ArrayList<ArrayList<String>> sl = getSL(s, allTuples);

        Set<ArrayList<String>> mPrime = getMPrime(sl);

        ArrayList<String> refinedQueries = new ArrayList<>();
        for(int k = 0; k < mPrime.size(); k++) {
            selectionAttributeValues.add(selectionAttributes);
            ArrayList<String> row = new ArrayList<>();
            for(int sa = 0; sa < selectionAttributes.size(); sa++) {
                for (int j = 0; j < allTuples.get(0).size(); j++) {
                    if (allTuples.get(0).get(j).equals(selectionAttributes.get(sa))) {
                        if(selectionAttributeOperators.get(sa).equals("<=") || selectionAttributeOperators.get(sa).equals("<")) {
                            double colMax = Double.MIN_VALUE;
                            for (ArrayList<String> mPrimeVal : mPrime) {
                                for (int i = 1; i < allTuples.size(); i++) {
                                    if (mPrimeVal.contains(allTuples.get(i).get(allTuples.get(0).size()))) {
                                        if (Double.parseDouble(allTuples.get(i).get(j)) > colMax) {
                                            colMax = Double.parseDouble(allTuples.get(i).get(j));
                                        }
                                    }
                                }
                            }
                            for (int l = 0; l < v.get(0).size(); l++) {
                                if (v.get(0).get(l).equals(allTuples.get(0).get(j))) {
                                    double maxVal = Math.max(Double.parseDouble(v.get(1).get(l)), colMax);
                                    if(selectionAttributeOperators.get(sa).equals("<")) {
                                        if(maxVal % 1 != 0) {
                                            maxVal = (int)maxVal;
                                        }
                                        else {
                                            maxVal += 1;
                                        }
                                    }
                                    if(maxVal % 1 == 0) {
                                        row.add(Integer.toString((int)maxVal));
                                    }
                                    else {
                                        row.add(Double.toString(maxVal));
                                    }
                                }
                            }
                        }
                        else {
                            double colMin = Double.MAX_VALUE;
                            for (ArrayList<String> mPrimeVal : mPrime) {
                                for (int i = 1; i < allTuples.size(); i++) {
                                    if (mPrimeVal.contains(allTuples.get(i).get(allTuples.get(0).size()))) {
                                        if (Double.parseDouble(allTuples.get(i).get(j)) < colMin) {
                                            colMin = Double.parseDouble(allTuples.get(i).get(j));
                                        }
                                    }
                                }
                            }
                            for (int l = 0; l < v.get(0).size(); l++) {
                                if (v.get(0).get(l).equals(allTuples.get(0).get(j))) {
                                    double minVal = Math.min(Double.parseDouble(v.get(1).get(l)), colMin);
                                    if(selectionAttributeOperators.get(sa).equals(">")) {
                                        if(minVal % 1 != 0) {
                                            minVal = (int)minVal;
                                        }
                                        else {
                                            minVal -= 1;
                                        }
                                    }
                                    if(minVal % 1 == 0) {
                                        row.add(Integer.toString((int)minVal));
                                    }
                                    else {
                                        row.add(Double.toString(minVal));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            selectionAttributeValues.add(row);
            String refinedQuery = getRefinedQuery(query);
            if(!refinedQueries.contains(refinedQuery)) {
                refinedQueries.add(refinedQuery);
            }
            selectionAttributeValues.clear();
        }

        ArrayList<ArrayList<String>> idealResults = new ArrayList<>();
        for (int i = 0; i < queryTuples.size(); i++) {
            idealResults.add(queryTuples.get(i));
        }

        for (ArrayList<String> whyNot : s) {
            idealResults.add(whyNot);
        }

        int idealCount = idealResults.size();

        for(int i = 0; i < refinedQueries.size(); i ++) {
            String refinedQuery = refinedQueries.get(i);
            ResultSet refinedQueryRS = stmt.executeQuery(refinedQuery);
            ArrayList<ArrayList<String>> refinedQueryTuples = new ArrayList<>();
            while(refinedQueryRS.next()) {
                ArrayList<String> row = new ArrayList<>();
                for(String attr: select) {
                    if(attr.contains(".")) {
                        attr = attr.split("\\.")[1];
                    }
                    row.add(attr);
                    row.add(refinedQueryRS.getString(attr));
                }
                refinedQueryTuples.add(row);
            }
            int rowCount = refinedQueryTuples.size();
            ArrayList<ArrayList<String>> irrelevantTuples = new ArrayList<>();
            for(int k = 0; k < rowCount; k++) {
                ArrayList<String> val = refinedQueryTuples.get(k);
                if(!containsVal(val, idealResults)) {
                    irrelevantTuples.add(val);
                }
            }
            if(!irrelevantTuples.isEmpty()) {
                String newRefinedQuery = addSelectionPredicates(refinedQuery, allTuples, queryTuples, s, select,
                        rowCount, idealCount, stmt, keys);
                refinedQueries.set(i, newRefinedQuery);
            }
        }

        return refinedQueries;
    }

    public static ArrayList<String> computeRefinedQueries(Statement stmt, ArrayList<ArrayList<String>> s,
                                                          HashMap<String, ArrayList> potentialJoins, Set<String> selectTables,
                                                          ArrayList<String> select, Set<String> keys, ArrayList<ArrayList<String>> ogS,
                                                          String query, ArrayList<ArrayList<String>> queryTuples) throws Exception {

        ArrayList<String> queries = getAllQueries(query, selectTables, potentialJoins);

        for(int i = 0; i < queries.size(); i++) {
            d += 12;
            String q = queries.get(i);
            qStarPhi = q.substring(0, 6) + " * " + q.substring(q.indexOf("FROM"));
            ArrayList<ArrayList<String>> allTuples = getAllTuples(stmt);
            for(ArrayList<String> whyNot: s) {
                if(!containsWhyNot(whyNot, allTuples)) {
                    queries.remove(q);
                    i--;
                    break;
                }
            }
        }

        ArrayList<String> refinedQueries = new ArrayList<>();
        if(queries.isEmpty()) {
            String from = Joiner.on(", ").join(selectTables);
            String qs = "SELECT " + Joiner.on(", ").join(select) + " FROM " + from;
            qStarPhi = qs.substring(0, 6) + " * " + qs.substring(qs.indexOf("FROM"));
            ArrayList<ArrayList<String>> allTuples = getAllTuples(stmt);
            for (int j = 0; j < allTuples.get(0).size(); j++) {
                String col = allTuples.get(0).get(j);
                if (!(keys.contains(col) || select.contains(col))) {
                    if(!attributeTypes.get(col).equals("VARCHAR")) {
                        selectionAttributes.add(col);
                        break;
                    }
                }
            }
            refinedQueries = computeRefinedQueries(stmt, qs, ogS, allTuples, queryTuples, select, keys);
            for(int i = 0; i < refinedQueries.size(); i++) {
                String newRefinedQuery = query + " UNION " + refinedQueries.get(i);
                refinedQueries.set(i, newRefinedQuery);
            }
        }
        else {
            for (String q : queries) {
                qStarPhi = q.substring(0, 6) + " * " + q.substring(q.indexOf("FROM"));
                ArrayList<ArrayList<String>> allTuples = getAllTuples(stmt);

                ArrayList<ArrayList<String>> idealResults = new ArrayList<>();
                for (ArrayList<String> whyNot : s) {
                    idealResults.add(whyNot);
                }

                int idealCount = idealResults.size();

                ResultSet qRS = stmt.executeQuery(q);
                ArrayList<ArrayList<String>> qTuples = new ArrayList<>();
                while(qRS.next()) {
                    ArrayList<String> row = new ArrayList<>();
                    for(String attr: select) {
                        row.add(attr);
                        row.add(qRS.getString(attr));
                    }
                    if(!containsVal(row, qTuples)) {
                        qTuples.add(row);
                    }
                }
                int rowCount = qTuples.size();
                ArrayList<ArrayList<String>> irrelevantTuples = new ArrayList<>();
                for(int k = 0; k < rowCount; k++) {
                    ArrayList<String> val = qTuples.get(k);
                    if(!containsVal(val, idealResults)) {
                        irrelevantTuples.add(val);
                    }
                }
                if(!irrelevantTuples.isEmpty()) {
                    q = addSelectionPredicates(q, allTuples, qTuples, s, select, rowCount, idealCount, stmt, keys);
                }

                refinedQueries.add(q);
            }
        }

        return refinedQueries;

    }

    public static ArrayList<ArrayList<String>> getAllTuples(Statement stmt) throws Exception {
        ArrayList<ArrayList<String>> allTuples = new ArrayList<>();
        ResultSet qStarPhiRS = stmt.executeQuery(qStarPhi);
        ResultSetMetaData qStarPhiMD = qStarPhiRS.getMetaData();
        int count = qStarPhiMD.getColumnCount();
        ArrayList<String> firstRow = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String columnLabel = qStarPhiMD.getColumnLabel(i);
            firstRow.add(columnLabel.toLowerCase());
            attributeTypes.put(columnLabel.toLowerCase(), qStarPhiMD.getColumnTypeName(i));
        }
        allTuples.add(firstRow);
        int tupleCount = 1;
        while (qStarPhiRS.next()) {
            ArrayList<String> row = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                row.add(qStarPhiRS.getString(i));
            }
            row.add("t" + tupleCount);
            tupleCount++;
            allTuples.add(row);
        }

        return allTuples;
    }

    public static boolean containsWhyNot(ArrayList<String> whyNot, ArrayList<ArrayList<String>> allTuples) {
        for (int i = 1; i < allTuples.size(); i++) {
            boolean isOkRow = true;
            for (int j = 0; j < allTuples.get(0).size(); j++) {
                for(int attrInd = 0; attrInd < whyNot.size(); attrInd += 2) {
                    String attr = whyNot.get(attrInd);
                    String val = whyNot.get(attrInd + 1);
                    if (allTuples.get(0).get(j).equals(attr)) {
                        if (!allTuples.get(i).get(j).equals(val)) {
                            isOkRow = false;
                            break;
                        }
                    }
                }
                if (!isOkRow) {
                    break;
                }
            }
            if (isOkRow) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsVal(ArrayList<String> val, ArrayList<ArrayList<String>> idealResults) {
        for(int i = 0; i < idealResults.size(); i++) {
            boolean isOkRow = true;
            ArrayList<String> idealResult = idealResults.get(i);
            for (int attrInd = 0; attrInd < idealResult.size(); attrInd += 2) {
                String attr = idealResult.get(attrInd);
                String value = idealResult.get(attrInd + 1);
                for (int attrInd2 = 0; attrInd2 < val.size(); attrInd2 += 2) {
                    if(val.get(attrInd2).equals(attr)){
                        if(!val.get(attrInd2 + 1).equals(value)) {
                            isOkRow = false;
                            break;
                        }
                    }
                }
            }
            if (isOkRow) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<ArrayList<String>> getSL(ArrayList<ArrayList<String>> s, ArrayList<ArrayList<String>> allTuples) throws Exception {
        ArrayList<ArrayList<String>> sl = new ArrayList<>();
        for(ArrayList<String> whyNot: s) {
            ArrayList<String> skylineTuples = getSkylineTuples(whyNot, allTuples);
            sl.add(skylineTuples);
            selectionAttributeValues.clear();
        }

        return sl;
    }

    public static Set<ArrayList<String>> getMPrime(ArrayList<ArrayList<String>> sl) {
        Set<ArrayList<String>> combinations = new HashSet<>();
        Set<ArrayList<String>> newCombinations;

        int index = 0;

        for(String i: sl.get(0)) {
            ArrayList<String> newList = new ArrayList<>();
            newList.add(i);
            combinations.add(newList);
        }
        index++;
        while(index < sl.size()) {
            ArrayList<String> nextList = sl.get(index);
            newCombinations = new HashSet<>();
            for(ArrayList<String> first: combinations) {
                for(String second: nextList) {
                    ArrayList<String> newList = new ArrayList<>();
                    newList.addAll(first);
                    newList.add(second);
                    newCombinations.add(newList);
                }
            }
            combinations = newCombinations;

            index++;
        }

        return combinations;
    }

    public static ArrayList<String> getAllQueries(String query, Set<String> selectTables, HashMap<String, ArrayList> potentialJoins) {
        ArrayList<String> queries = new ArrayList<>();
        String querySelect = query.substring(0, query.indexOf("FROM") - 1);
        for(String selectTable: selectTables) {
            ArrayList<ArrayList<String>> tables = potentialJoins.get(selectTable);
            String from = selectTable;
            String where = "";
            String newQuery;
            for(int i = 0; i < tables.size(); i++) {
                for(int j = i; j < tables.size(); j++) {
                    if (j == i) {
                        from = selectTable + ", " + tables.get(j).get(0);
                        where = selectTable + "." + tables.get(j).get(1) + " = "
                                + tables.get(j).get(0) + "." + tables.get(j).get(2);
                    } else {
                        from = from + ", " + tables.get(j).get(0);
                        where = where + " AND " + selectTable + "." + tables.get(j).get(1) + " = "
                                + tables.get(j).get(0) + "." + tables.get(j).get(2);
                    }
                    newQuery = querySelect + " FROM " + from + " WHERE " + where;
                    queries.add(newQuery);
                }
            }
        }

        return queries;
    }

    public static ArrayList<String> getMutatedQueries(Statement stmt, String query, ArrayList<String> mutationAttrValues,
                                                      int noOfMutations, HashMap<String, ArrayList> potentialJoins) throws Exception {
        Set<String> attrTables = new HashSet<>();
        Set<String> attrValues = new HashSet<>();
        HashMap<String, String> attrOperators = new HashMap();
        for(String str : mutationAttrValues) {
            attrTables.add(str.split("\\.")[0]);
            String attr = str.split("->")[0].split("\\.")[1];
            String values = str.split("->")[1].split(";")[0];
            String operator = str.split(";")[1];
            attrOperators.put(attr.trim(), operator.trim());
            for(String value : values.split(" ")) {
                if(!value.equals("")) {
                    attrValues.add(attr + " " + value);
                }
            }
        }

        Select selectStatement = (Select) CCJSqlParserUtil.parse(query);
        PlainSelect plainSelect = (PlainSelect) selectStatement.getSelectBody();
        ArrayList<String> select = new ArrayList();
        for(SelectItem sel: plainSelect.getSelectItems()) {
            String sa = sel.toString();
            if(sa.contains(".")) {
                sa = sa.split("\\.")[1];
            }
            select.add(sa);
        }

        Set<String> selectTables = new HashSet<>();
        for(String table: attrTables) {
            ResultSet tableColumnNames = stmt.executeQuery("SELECT column_name FROM information_schema.columns "
                    + "WHERE table_name = '" + table + "'");
            while (tableColumnNames.next()) {
                if (select.contains(tableColumnNames.getString(1).toLowerCase())) {
                    selectTables.add(table);
                }
            }
        }

        ArrayList<String> queries = getAllQueries(query, selectTables, potentialJoins);
        Set<Set<String>> attrValuesSets = Sets.combinations(attrValues, noOfMutations);
        ArrayList<String> newQueries = new ArrayList<>();
        for(int i = 0; i < queries.size(); i++) {
            for(Set<String> attrValueSet : attrValuesSets) {
                String q = queries.get(i);
                for(String attrValue: attrValueSet) {
                    String attr = attrValue.split(" ")[0];
                    String value = attrValue.split(" ")[2];
                    if (!q.contains(attr)) {
                        q = q + " AND " + attr + " " + attrOperators.get(attr) + " " + value;
                        newQueries.add(q);
                    }
                }
            }
        }

        return newQueries;
    }

    public static boolean isValid(ArrayList<ArrayList<String>> s, ArrayList<ArrayList<String>> queryTuples,
                                  ArrayList<ArrayList<String>> refinedQueryTuples) {
        boolean validated = true;

        for (ArrayList<String> whyNot : s) {
            if (!containsVal(whyNot, refinedQueryTuples)) {
                validated = false;
            }
        }
        for(ArrayList<String> val: queryTuples) {
            if(!containsVal(val, refinedQueryTuples)) {
                validated = false;
            }
        }

        return validated;
    }

    public static ArrayList<ArrayList<String>> getS(ArrayList<ArrayList<String>>  originalQueryTuples,
                                                    ArrayList<ArrayList<String>> queryTuples) throws Exception {
        ArrayList<ArrayList<String>> s = new ArrayList<>();
        for(int k = 0; k < originalQueryTuples.size(); k++) {
            ArrayList<String> val = originalQueryTuples.get(k);
            if (!containsVal(val, queryTuples)) {
                s.add(val);
            }
        }

        return s;
    }

    public static void main(String args[]) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter MySQL user: ");
        String user = br.readLine();
        System.out.print("Enter MySQL password: ");
        String password = br.readLine();
        System.out.print("Enter MySQL schema name: ");
        String database = br.readLine();
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/"+database+"?verifyServerCertificate=false&useSSL=true", user, password);
        java.sql.Statement stmt = con.createStatement();

        ArrayList<String> tableNames = new ArrayList<>();
        ResultSet tableNamesRS = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = '" + database + "'");
        while(tableNamesRS.next()) {
            tableNames.add(tableNamesRS.getString(1));
        }

        HashMap<String, ArrayList> potentialJoins = new HashMap<>();
        for(int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.get(i);
            ResultSet stats = stmt.executeQuery("select * from INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "where TABLE_NAME = '" + tableName + "'");
            while(stats.next()) {
                String table = stats.getString(11);
                ArrayList<ArrayList<String>> values = new ArrayList<>();
                ArrayList<ArrayList<String>> revValues = new ArrayList<>();
                if(tableNames.contains(table)) {
                    ArrayList<String> value = new ArrayList<>();
                    ArrayList<String> revValue = new ArrayList<>();

                    value.add(tableName);
                    value.add(stats.getString(12));
                    value.add(stats.getString(7));
                    values.add(value);

                    revValue.add(table);
                    revValue.add(stats.getString(7));
                    revValue.add(stats.getString(12));
                    revValues.add(revValue);

                    if(potentialJoins.get(table) == null) {
                        potentialJoins.put(table, values);
                    }
                    else {
                        potentialJoins.get(table).add(value);
                    }

                    if(potentialJoins.get(tableName) == null) {
                        potentialJoins.put(tableName, revValues);
                    }
                    else {
                        ArrayList<ArrayList<String>> newValues = potentialJoins.get(tableName);
                        boolean flag = true;
                        for(ArrayList<String> val: newValues) {
                            if(val.get(0).equals(table)) {
                                flag = false;
                            }
                        }
                        if(flag) {
                            potentialJoins.get(tableName).add(revValue);
                        }
                    }
                }
            }
        }

        System.out.print("Enter original query: "); // SELECT title FROM movie_test m, movie_actor_test a WHERE m.id = a.movie AND startYear <= 1960
        String originalQuery = br.readLine();
        System.out.print("Enter number of mutation rules: "); // 1
        int noOfMutations = Integer.parseInt(br.readLine());
        ArrayList<String> mutationAttrValues = new ArrayList<>();
        for(int i = 0; i < noOfMutations; i++) {
            System.out.print("Enter mutation rule: ");
            mutationAttrValues.add(br.readLine()); // movie_test.startYear -> 1920 1950 1980 2010; <=
        }
        ArrayList<String> mutatedQueries = getMutatedQueries(stmt, originalQuery, mutationAttrValues, noOfMutations, potentialJoins);

        long startTime = System.currentTimeMillis();
        System.out.println();
        System.out.println("Original Query: " + originalQuery);

        Random rand = new Random();
        String query;
        PlainSelect plainSelect;
        ArrayList<String> select = new ArrayList();
        ArrayList<ArrayList<String>> originalQueryTuples;
        ArrayList<ArrayList<String>> queryTuples;
        ArrayList<ArrayList<String>> s;
        do {
            query = mutatedQueries.get(rand.nextInt(mutatedQueries.size()));

            Select selectStatement = (Select) CCJSqlParserUtil.parse(query);
            plainSelect = (PlainSelect) selectStatement.getSelectBody();
            for(SelectItem sel: plainSelect.getSelectItems()) {
                String sa = sel.toString();
                if(sa.contains(".")) {
                    sa = sa.split("\\.")[1];
                }
                select.add(sa);
            }

            originalQueryTuples = new ArrayList<>();
            ResultSet originalQueryRS = stmt.executeQuery(originalQuery);
            while(originalQueryRS.next()) {
                ArrayList<String> row = new ArrayList<>();
                for(String attr: select) {
                    if(attr.contains(".")) {
                        attr = attr.split("\\.")[1];
                    }
                    row.add(attr);
                    row.add(originalQueryRS.getString(attr));
                }
                if(!containsVal(row, originalQueryTuples)) {
                    originalQueryTuples.add(row);
                }
            }

            ResultSet queryRS = stmt.executeQuery(query);
            queryTuples = new ArrayList<>();
            while(queryRS.next()) {
                ArrayList<String> row = new ArrayList<>();
                for(String attr: select) {
                    if(attr.contains(".")) {
                        attr = attr.split("\\.")[1];
                    }
                    row.add(attr);
                    row.add(queryRS.getString(attr));
                }
                if(!containsVal(row, queryTuples)) {
                    queryTuples.add(row);
                }
            }

            s = getS(originalQueryTuples, queryTuples);
        } while(s.isEmpty());

        System.out.println("Modified Query: " + query);

        double percentage = rand.nextFloat();
        int size = s.size();
        while(!(s.size() == Math.round(percentage * size))) {
            int removeInd = rand.nextInt(s.size());
            s.remove(removeInd);
        }

        System.out.println("Percentage: " + Math.round(percentage * 100) + "%");
        System.out.println("Why-Not Questions: (" + s.size() + ")");
        for (ArrayList<String> whyNot : s) {
            for (int i = 0; i < whyNot.size(); i += 2) {
                System.out.print(whyNot.get(i) + " -> " + whyNot.get(i + 1) + ";");
            }
            System.out.println();
        }

        ArrayList<String> fromTables = new ArrayList<>();
        FromItem from = plainSelect.getFromItem();
        fromTables.add(from.toString().split(" ")[0]);
        Expression whereClause = plainSelect.getWhere();
        qStarPhi = qStarPhi + from;

        Set<String> keys = new HashSet<>();
        for (int i = 0; i < tableNames.size(); i++) {
            ResultSet keysRS = stmt.executeQuery("select * from INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                    "where TABLE_NAME = '" + tableNames.get(i) + "'");
            while (keysRS.next()) {
                keys.add(keysRS.getString(12));
                keys.add(keysRS.getString(7));
            }
        }

        List<Join> currentJoins = plainSelect.getJoins();

        if (currentJoins != null) {
            for (int i = 0; i < currentJoins.size(); i++) {
                qStarPhi = qStarPhi + ", " + currentJoins.get(i);
                fromTables.add(currentJoins.get(i).toString().split(" ")[0]);
            }
            whereClause.accept(new ExpressionVisitorAdapter() {
                @Override
                public void visit(AndExpression expr) {
                    if (expr.getLeftExpression() instanceof AndExpression) {
                        expr.getLeftExpression().accept(this);
                    } else {
                        if ((expr.getLeftExpression() instanceof EqualsTo)) {
                            qStarPhi = qStarPhi + " WHERE " + expr.getLeftExpression();
                            qStar = qStarPhi;
                        }
                        if ((expr.getRightExpression() instanceof EqualsTo)) {
                            qStarPhi = qStarPhi + " AND " + expr.getRightExpression();
                            qStar = qStarPhi;
                        }
                    }
                    if (expr.getRightExpression() instanceof MinorThanEquals) {
                        qStar = qStar + " AND " + expr.getRightExpression();
                    } else if (expr.getRightExpression() instanceof MinorThan) {
                        qStar = qStar + " AND " + expr.getRightExpression();
                    } else if (expr.getRightExpression() instanceof GreaterThanEquals) {
                        qStar = qStar + " AND " + expr.getRightExpression();
                    } else {
                        qStar = qStar + " AND " + expr.getRightExpression();
                    }
                }
            });
        } else {
            if (whereClause != null) {
                qStar = qStarPhi + " WHERE " + whereClause;
            }
        }

        if (whereClause != null) {
            Expression expr = CCJSqlParserUtil.parseCondExpression(whereClause.toString());
            expr.accept(new ExpressionVisitorAdapter() {
                @Override
                protected void visitBinaryExpression(BinaryExpression expr) {
                    if (expr instanceof ComparisonOperator) {
                        if (!(expr.getRightExpression() instanceof Column)) {
                            selectionAttributes.add(expr.getLeftExpression().toString().toLowerCase());
                            selectionAttributeOperators.add(expr.getStringExpression());
                        }
                    }
                    super.visitBinaryExpression(expr);
                }
            });
        }

        Set<String> selectTables = new HashSet<>();
        for (String table : fromTables) {
            ResultSet tableColumnNames = stmt.executeQuery("SELECT column_name FROM information_schema.columns "
                    + "WHERE table_name = '" + table + "'");
            while (tableColumnNames.next()) {
                if (select.contains(tableColumnNames.getString(1).toLowerCase())) {
                    selectTables.add(table);
                }
            }
        }

        ArrayList<ArrayList<String>> allTuples = getAllTuples(stmt);

        boolean schemaFlag = false;
        for (ArrayList<String> whyNot : s) {
            if (!containsWhyNot(whyNot, allTuples)) {
                schemaFlag = true;
                break;
            }
        }

        ArrayList<ArrayList<String>> ogS = new ArrayList<>(s);
        ArrayList<String> refinedQueries;
        if (!schemaFlag) {
            refinedQueries = computeRefinedQueries(stmt, query, s, allTuples, queryTuples, select, keys);
        } else {
            for (int i = 0; i < queryTuples.size(); i++) {
                s.add(queryTuples.get(i));
            }

            selectionAttributes.clear();
            refinedQueries = computeRefinedQueries(stmt, s, potentialJoins, selectTables, select, keys, ogS, query, queryTuples);
        }

        System.out.println("Tuples in OQ: " + originalQueryTuples.size());
        System.out.println("Refined Queries: ");
        int queryCount = 1;
        for (String refinedQuery : refinedQueries) {
            System.out.println(queryCount + ") \t" + refinedQuery);
            ResultSet refinedQueryRS = stmt.executeQuery(refinedQuery);
            ArrayList<ArrayList<String>> refinedQueryTuples = new ArrayList<>();
            while (refinedQueryRS.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (String attr : select) {
                    if (attr.contains(".")) {
                        attr = attr.split("\\.")[1];
                    }
                    row.add(attr);
                    row.add(refinedQueryRS.getString(attr));
                }
                if (!containsVal(row, refinedQueryTuples)) {
                    refinedQueryTuples.add(row);
                }
            }

            boolean validated = isValid(ogS, queryTuples, refinedQueryTuples);

            if (validated) {
                System.out.println("\tValid: Yes");
            } else {
                System.out.println("\tValid: No");
            }

            int truePositives = 0;
            int falseNegatives = 0;
            for (ArrayList<String> val : originalQueryTuples) {
                if (!containsVal(val, refinedQueryTuples)) {
                    falseNegatives++;
                } else {
                    truePositives++;
                }
            }

            int falsePositives = 0;
            for (ArrayList<String> val : refinedQueryTuples) {
                if (!containsVal(val, originalQueryTuples)) {
                    falsePositives++;
                }
            }

            System.out.println("\tNo. of true positives (w.r.t the original query): " + truePositives);
            System.out.println("\tNo. of false positives (w.r.t the original query): " + falsePositives);
            System.out.println("\tNo. of false negatives (w.r.t the original query): " + falseNegatives);

            System.out.println("\td: " + d);
            System.out.println("\ti: " + (refinedQueryTuples.size() - (queryTuples.size() + ogS.size())));

            queryCount++;
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Time: " + elapsedTime / 1000.0 + " seconds");

        con.close();
    }
}