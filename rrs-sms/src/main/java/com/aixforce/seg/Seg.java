package com.aixforce.seg;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Seg {
    private final static Logger log = LoggerFactory.getLogger(Seg.class);

    private Map<Character, Map> d = new TreeMap<Character, Map>();
    private Set<Character> stopWords = new HashSet<Character>();

    public void useDefaultDict() throws Exception {
        try {
            Set<String> words = Resources.readLines(Resources.getResource("dic/main.dic"), Charsets.UTF_8, new LineProcessor<Set<String>>() {
                private Set<String> words = Sets.newHashSet();

                @Override
                public boolean processLine(String s) throws IOException {
                    if (!Strings.isNullOrEmpty(s) && s.trim().length() <= 4) {
                        words.add(s);
                    }
                    return true;
                }

                @Override
                public Set<String> getResult() {
                    return words;
                }
            });

            set(words);

            Resources.readLines(Resources.getResource("dic/suffix.dic"), Charsets.UTF_8, new LineProcessor<Set<Character>>() {
                @Override
                public boolean processLine(String s) throws IOException {
                    if (!Strings.isNullOrEmpty(s)) {
                        stopWords.add(s.trim().charAt(0));
                    }
                    return true;
                }

                @Override
                public Set<Character> getResult() {
                    return null;
                }
            });

        } catch (Exception e) {
            log.error("failed to load dicts", e);
            throw new RuntimeException(e);
        }
    }

    public void set(Set<String> words) {
        Map<Character, Map> p;
        Map<Character, Map> q = Maps.newHashMap();
        Character k = null;
        for (String word : words) {
            word = (char) 11 + word;
            p = d;
            for (int i = word.length() - 1; i >= 0; i--) {
                Character cc = Character.toLowerCase(word.charAt(i));
                if (p == null) {
                    q.put(k, new TreeMap<Character, Object>());
                    p = q.get(k);
                }
                if (!p.containsKey(cc)) {
                    p.put(cc, null);
                    q = p;
                    k = cc;
                }
                p = p.get(cc);
            }
        }

    }

    private List<String> _binary_seg(String s) {
        int ln = s.length();
        List<String> R = new ArrayList<String>();
        if (ln == 1) {
            R.add(s);
            return R;
        }
        for (int i = ln; i > 1; i--) {
            String tmp = s.substring(i - 2, i);
            R.add(tmp);
        }
        return R;
    }

    private List<String> findAll(String pattern, String text) {
        List<String> R = new ArrayList<String>();
        Matcher mc = Pattern.compile(pattern).matcher(text);
        while (mc.find()) {
            R.add(mc.group(1));
        }
        return R;
    }

    private List<String> _pro_unreg(String piece) {
        List<String> R = new ArrayList<String>();
        String[] tmp = piece.replaceAll("。|，|,|！|…|!|《|》|<|>|\"|'|:|：|？|\\?|、|\\||“|”|‘|’|；|—|（|）|·|\\(|\\)|　", " ").split("\\s");
        Cutter spliter = new Cutter("([0-9A-Za-z\\-\\+#@_\\.]+)", true);
        for (int i = tmp.length - 1; i > -1; i--) {
            String[] mc = spliter.split(tmp[i]);
            for (int j = mc.length - 1; j > -1; j--) {
                String r = mc[j];
                if (Pattern.matches("([0-9A-Za-z\\-\\+#@_\\.]+)", r))
                    R.add(r);
                else
                    R.addAll(_binary_seg(r));
            }
        }
        return R;
    }

    public List<String> cut(String text) {
        Map<Character, Map> p = d;
        int ln = text.length();
        int i = ln;
        int j = 0;
        int z = ln;
        int q = 0;
        List<String> recognised = new ArrayList<String>();
        Integer[] mem = null;
        Integer[] mem2 = null;

        while (i - j > 0) {
            Character t = Character.toLowerCase(text.charAt(i - 1 - j));
            if (!p.containsKey(t)) {
                if (mem != null || mem2 != null) {
                    if (mem != null) {
                        i = mem[0];
                        j = mem[1];
                        z = mem[2];
                        mem = null;
                    } else if (mem2 != null) {
                        int delta = mem2[0] - i;
                        if (delta >= 1) {
                            if (delta < 5 && Pattern.matches("[\\w\\u2E80-\\u9FFF]", t.toString())) {
                                Character pre = text.charAt(i - j);
                                if (!stopWords.contains(pre)) {
                                    i = mem2[0];
                                    j = mem2[1];
                                    z = mem2[2];
                                    q = mem2[3];
                                    while (recognised.size() > q) {
                                        recognised.remove(recognised.size() - 1);
                                    }
                                }
                            }
                            mem2 = null;
                        }
                    }
                    p = d;
                    if (i < ln && i < z) {
                        List<String> unreg_tmp = _pro_unreg(text.substring(i, z));
                        recognised.addAll(unreg_tmp);
                    }
                    recognised.add(text.substring(i - j, i));
                    i = i - j;
                    z = i;
                    j = 0;
                    continue;
                }
                j = 0;
                i--;
                p = d;
                continue;
            }
            p = p.get(t);
            j++;
            if (p.containsKey((char) 11)) {
                if (j <= 2) {
                    mem = new Integer[]{i, j, z};
                    Character xsuffix = text.charAt(i - 1);
                    if ((z - i < 2) && stopWords.contains(xsuffix) && (mem2 == null || (mem2 != null && mem2[0] - i > 1))) {
                        mem = null;
                        mem2 = new Integer[]{i, j, z, recognised.size()};
                        p = d;
                        i--;
                        j = 0;
                    }
                    continue;
                }
                p = d;
                if (i < ln && i < z) {
                    List<String> unreg_tmp = _pro_unreg(text.substring(i, z));
                    recognised.addAll(unreg_tmp);
                }
                recognised.add(text.substring(i - j, i));
                i = i - j;
                z = i;
                j = 0;
                mem = null;
                mem2 = null;
            }
        }
        if (mem != null) {
            i = mem[0];
            j = mem[1];
            z = mem[2];
            recognised.addAll(_pro_unreg(text.substring(i, z)));
            recognised.add(text.substring(i - j, i));
        } else
            recognised.addAll(_pro_unreg(text.substring(i - j, z)));

        return recognised;
    }
}
