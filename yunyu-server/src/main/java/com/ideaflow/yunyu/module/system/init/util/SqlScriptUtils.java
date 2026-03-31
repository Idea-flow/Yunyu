package com.ideaflow.yunyu.module.system.init.util;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 脚本解析工具类。
 * 作用：将初始化 SQL 脚本按语句拆分，便于初始化流程逐条执行。
 */
public final class SqlScriptUtils {

    private SqlScriptUtils() {
    }

    /**
     * 将完整 SQL 脚本拆分为多条语句。
     *
     * @param script 完整 SQL 脚本
     * @return SQL 语句列表
     */
    public static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        if (script == null || script.isBlank()) {
            return statements;
        }

        StringBuilder current = new StringBuilder();
        for (String rawLine : script.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("--")) {
                continue;
            }
            current.append(rawLine).append('\n');
            if (line.endsWith(";")) {
                String statement = current.toString().trim();
                if (!statement.isEmpty()) {
                    statements.add(statement.substring(0, statement.length() - 1));
                }
                current.setLength(0);
            }
        }

        String tail = current.toString().trim();
        if (!tail.isEmpty()) {
            statements.add(tail);
        }
        return statements;
    }
}
