package net.michalfoksa.mqtt2influxdb.util;

import static org.junit.Assert.assertEquals;
import net.michalfoksa.mqtt2influxdb.util.StringUtils;

import org.junit.Test;

public class WildcardToRegex_Test {

        @Test
        public void star_becomes_dot_star() throws Exception {
            assertEquals("^gl.*b$", StringUtils.wildcardToRegex("gl*b"));
        }

        @Test
        public void dolar_becomes_slash_dolar() throws Exception {
            assertEquals("^\\$SYS/broker/wildcard_subscriptions/count$", StringUtils.wildcardToRegex("$SYS/broker/wildcard_subscriptions/count"));
        }

        @Test
        public void escaped_star_is_unchanged() throws Exception {
            assertEquals("^gl\\*b$", StringUtils.wildcardToRegex("gl\\*b"));
        }

        @Test
        public void question_mark_becomes_dot() throws Exception {
            assertEquals("^gl.b$", StringUtils.wildcardToRegex("gl?b"));
        }

        @Test
        public void escaped_question_mark_is_unchanged() throws Exception {
            assertEquals("^gl\\?b$", StringUtils.wildcardToRegex("gl\\?b"));
        }

        @Test
        public void square_bracket_becomes_slash_bracket() throws Exception {
            assertEquals("^gl\\[-o\\]b$", StringUtils.wildcardToRegex("gl[-o]b"));
        }

        @Test
        public void escaped_classes_are_escaped_once_again() throws Exception {
            assertEquals("^gl\\\\\\[-o\\\\\\]b$", StringUtils.wildcardToRegex("gl\\[-o\\]b"));
        }

        @Test
        public void escape_carat_if_it_is_the_first_char_in_a_character_class() throws Exception {
            assertEquals("^gl\\[\\^o\\]b$", StringUtils.wildcardToRegex("gl[^o]b"));
        }

        @Test
        public void metachars_are_escaped() throws Exception {
            assertEquals("^gl..*\\.\\(\\)\\+\\|\\^\\$\\@\\%b$", StringUtils.wildcardToRegex("gl?*.()+|^$@%b"));
        }

        @Test
        public void escaped_backslash_is_unchanged() throws Exception {
            assertEquals("^gl\\\\b$", StringUtils.wildcardToRegex("gl\\\\b"));
        }

        @Test
        public void slashQ_and_slashE_are_escaped() throws Exception {
            assertEquals("^\\\\Qglob\\\\E$", StringUtils.wildcardToRegex("\\Qglob\\E"));
        }
    }