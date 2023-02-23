package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class WikiPhilosophy {

    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = new WikiFetcher();

    Node bracket;

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     * <p>
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     * <p>
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     * that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        String currentSource = source;
        String basePath = "https://en.wikipedia.org";

        for (int i = 0; i < limit; i++) {
            visited.add(currentSource);
            Elements paragraphs = wf.fetchWikipedia(currentSource);
            currentSource = basePath + findFirstValidLink(paragraphs);

            if (currentSource.equals(destination)) {
                System.out.printf("Philosophy found after %d iterations%n", i);
            }
        }

        visited.forEach(System.out::println);
    }

    private static Node recursiveDFS(Node root) {
        if (!(root instanceof TextNode)) {
            Element element = (Element) root;
            if ("a".equals(element.tag().getName())) {
                return element;
            }
        }

        for (Node node : root.childNodes()) {
            if (node instanceof TextNode) {
                continue;
            }

            Node href = recursiveDFS(node);

            if (isValidLink(href)) {
                return href;
            }
        }

        return null;
    }

    private static String findFirstValidLink(Elements paragraphs) {
        for (Element paragraph : paragraphs) {
            Node nodeContainedLink = recursiveDFS(paragraph);

            if (nodeContainedLink != null) {
                return nodeContainedLink.attributes().get("href");
            }
        }

        return null;
    }

    private static boolean isValidLink(Node nodeContainedLink) {
        if (nodeContainedLink == null) {
            return false;
        }

        String href = nodeContainedLink.attributes().get("href");

        if (href.startsWith("#")) {
            return false;
        }

        if (!href.startsWith("/")) {
            return false;
        }

        Node previousSibling = nodeContainedLink.previousSibling();

        if (previousSibling instanceof TextNode) {
            Deque<Character> characters = new ArrayDeque<>();
            String text = ((TextNode) previousSibling).text();

            for (int i = text.length() - 1; i >= 0; i--) {
                if (characters.isEmpty()) {
                    if (text.charAt(i) == ')' || text.charAt(i) == '(') {
                        characters.push(text.charAt(i));
                    }
                } else {
                    if (text.charAt(i) == ')') {
                        if (characters.peek() == '(') {
                            characters.pop();
                        } else {
                            characters.push(')');
                        }
                    } else if (text.charAt(i) == '('){
                        if (characters.peek() == ')') {
                            characters.pop();
                        } else {
                            characters.push('(');
                        }
                    }
                }
            }

            if (!characters.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
