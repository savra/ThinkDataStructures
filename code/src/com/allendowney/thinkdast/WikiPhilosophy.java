package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
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

        Elements elements = wf.fetchWikipedia(source);

        for (Element node : elements) {
            if (recursiveDFS(node) != null) {
                System.out.println("Philosophy found");
            } else {
                System.out.println("Philosophy not found");
            }
        }

        visited.forEach(System.out::println);
    }

    private static Node recursiveDFS(Node root) {
        if (!(root instanceof TextNode)) {
            Element element = (Element) root;
            if ("a".equals(element.tag().getName())) {
                if (element.attributes().get("href").endsWith("/wiki/Philosophy")) {
                    visited.add(element.attributes().get("href"));
                    return element;
                } else {
                    visited.add(element.attributes().get("href"));
                }
            }
        }

        for (Node node : root.childNodes()) {
            Node targetNode = recursiveDFS(node);

            if (targetNode != null) {
                if (targetNode.parent().getClass() == Element.class) {
                    if (targetNode.childNodeSize() == 1
                            && targetNode.childNode(0).getClass() == TextNode.class
                            && ((TextNode) targetNode.childNode(0)).text().startsWith("P")) {
                        if (!((Element) targetNode.parent()).tag().getName().equals("i")) {

                            int i = targetNode.siblingIndex();

                            if (i == 0 || i == targetNode.parent().childNodeSize() - 1
                                    || (targetNode.parent().childNode(i - 1).getClass() == TextNode.class
                                    && targetNode.parent().childNode(i + 1).getClass() == TextNode.class)) {
                                TextNode prev = (TextNode) targetNode.parent().childNode(i - 1);
                                TextNode next = (TextNode) targetNode.parent().childNode(i + 1);

                                if (prev.text().charAt(prev.text().length() - 1) == '('
                                        && next.text().charAt(0) == ')') {
                                    return targetNode;
                                }
                            }
                        }
                    }
                }
            }

            return targetNode;
        }

        return null;
    }
}
