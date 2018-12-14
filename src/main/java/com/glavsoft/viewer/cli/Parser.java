package com.glavsoft.viewer.cli;

import com.glavsoft.utils.Strings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final Map options = new LinkedHashMap();
    private final List plainOptions = new ArrayList();
    private boolean isSetPlainOptions = false;

    public Parser() {
    }

    public void addOption(String opName, String defaultValue, String desc) {
        Parser.Option op = new Parser.Option(opName, defaultValue, desc);
        this.options.put(opName.toLowerCase(), op);
    }

    public void parse(String[] args) {
        String[] arr$ = args;
        int len$ = args.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            String p = arr$[i$];
            if (p.startsWith("-")) {
                int skipMinuses = p.startsWith("--") ? 2 : 1;
                String[] params = p.split("=", 2);
                Parser.Option op = (Parser.Option) this.options.get(params[0].toLowerCase().substring(skipMinuses));
                if (op != null) {
                    op.isSet = true;
                    if (params.length > 1 && !Strings.isTrimmedEmpty(params[1])) {
                        op.value = params[1];
                    }
                }
            } else if (!p.startsWith("-")) {
                this.isSetPlainOptions = true;
                this.plainOptions.add(p);
            }
        }

    }

    public String getValueFor(String param) {
        Parser.Option op = (Parser.Option) this.options.get(param.toLowerCase());
        return op != null ? op.value : null;
    }

    public boolean isSet(String param) {
        Parser.Option op = (Parser.Option) this.options.get(param.toLowerCase());
        return op != null && op.isSet;
    }

    public boolean isSetPlainOptions() {
        return this.isSetPlainOptions;
    }

    public String getPlainOptionAt(int index) {
        return (String) this.plainOptions.get(index);
    }

    public int getPlainOptionsNumber() {
        return this.plainOptions.size();
    }

    public String optionsUsage() {
        StringBuilder sb = new StringBuilder();
        int maxNameLength = 0;

        Iterator i$;
        Parser.Option op;
        for (i$ = this.options.values().iterator(); i$.hasNext(); maxNameLength = Math.max(maxNameLength, op.opName.length())) {
            op = (Parser.Option) i$.next();
        }

        i$ = this.options.values().iterator();

        while (i$.hasNext()) {
            op = (Parser.Option) i$.next();
            sb.append(" -").append(op.opName);

            for (int i = 0; i < maxNameLength - op.opName.length(); ++i) {
                sb.append(' ');
            }

            sb.append(" : ").append(op.desc).append('\n');
        }

        return sb.toString();
    }

    private static class Option {
        protected String opName;
        protected String defaultValue;
        protected String desc;
        protected String value;
        protected boolean isSet = false;

        public Option(String opName, String defaultValue, String desc) {
            this.opName = opName;
            this.defaultValue = defaultValue;
            this.desc = desc;
            this.value = defaultValue;
        }
    }
}
