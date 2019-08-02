package com.redsquare.citizen.debug.shell;

import java.util.Scanner;

public class DebuggerShell {
  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_BOLD = "\u001B[1m";
  private static final String ANSI_RED = "\u001B[31m";
  private static final String ANSI_GREEN = "\u001B[32m";
  private static final String ANSI_YELLOW = "\u001B[33m";
  private static final String ANSI_BLUE = "\u001B[34m";

  private static final Scanner in = new Scanner(System.in);

  public static void launch() {
    prompt();
  }

  private static void prompt() {
    System.out.print(ANSI_GREEN + ANSI_BOLD + "[Jordan] > " + ANSI_RESET);
  }
}
