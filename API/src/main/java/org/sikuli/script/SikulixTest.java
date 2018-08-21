/*
 * Copyright (c) 2010-2018, sikuli.org, sikulix.com - MIT license
 */

package org.sikuli.script;

import org.sikuli.basics.Settings;
import org.sikuli.util.ScreenHighlighter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SikulixTest {

  //<editor-fold desc="housekeeping">
  private static Screen scr = new Screen();

  private static long start = -1;

  private static void timer() {
    start = new Date().getTime();
  }

  private static void timer(String msg) {
    p("%d (%s)", new Date().getTime() - start, msg.isEmpty() ? "msec" : msg);
  }

  private static void p(String msg, Object... args) {
    if (msg.isEmpty()) {
      return;
    }
    System.out.println(String.format(msg, args));
  }

  private static String showBase = "API/src/main/resources/ImagesAPI";
  private static String showLink;
  private static int showWait;
  private static int showBefore;
  private static boolean isShown = false;

  private static void show(String image) {
    show(image, 3, 0);
  }

  private static void show(String image, int wait) {
    show(image, wait, 0);
  }

  private static void showStop() {
    if (isShown) {
      scr.type("w", keyMeta);
      isShown = false;
    }
  }

  private static void show(String image, int wait, int before) {
    if (!image.endsWith(".png")) {
      image += ".png";
    }
    showLink = "file://" + Image.create(image).getFileURL().getPath();
    showWait = wait;
    showBefore = before;
    Thread runnable = new Thread() {
      @Override
      public void run() {
        if (before > 0) {
          RunTime.pause(showBefore);
        }
        App.openLink(showLink);
        if (wait > 0) {
          RunTime.pause(showWait);
          //p("%s", App.focusedWindow());
          scr.type("w", Key.CMD);
        } else {
          isShown = true;
        }
      }
    };
    runnable.start();
  }

  private static RunTime runTime = RunTime.get();
  private static Region reg = null;
  private static Region regWin = null;

  public static boolean openTestPage() {
    return openTestPage("");
  }

  private static String keyMeta = Key.CTRL;
  private static boolean isBrowserRunning = false;

  public static boolean openTestPage(String page) {
    String testPageBase = "https://github.com/RaiMan/SikuliX1/wiki/";
    String testPage = "Test-page-text";
    if (!page.isEmpty()) {
      testPage = page;
    }
    String actualPage = testPageBase + testPage;
    boolean success = false;
    String corner = "apple";
    Pattern pCorner = new Pattern(corner).similar(0.9);
    Match cornerSeen = null;
    if (App.openLink(actualPage)) {
      scr.wait(1.0);
      Screen allScreen = Screen.all();
      if (Do.SX.isNotNull(allScreen.exists(pCorner, 30))) {
        success = true;
        cornerSeen = allScreen.getLastMatch();
        cornerSeen.hover();
        reg = App.focusedWindow();
        regWin = new Region(reg);
      }
    }
    if (success) {
      int wheelDirection = 0;
      success = false;
      while (!success) {
        List<Match> matches = reg.getAll(corner);
        if (matches.size() == 2) {
          reg = matches.get(0).union(matches.get(1));
          reg.h += 5;
          success = true;
          break;
        }
        if (wheelDirection == 0) {
          wheelDirection = Button.WHEEL_DOWN;
          reg.wheel(wheelDirection, 1);
          scr.wait(0.5);
          Match cornerMatch = regWin.exists(pCorner);
          if (cornerMatch.y >= cornerSeen.y) {
            wheelDirection *= -1;
          }
        }
        reg.wheel(wheelDirection, 1);
        scr.wait(0.5);
      }
    }
    if (!success) {
      p("***** Error: web page did not open (30 secs)");
    } else {
      //reg.highlight(1);
      isBrowserRunning = true;
    }
    return success;
  }

  private static void browserStop() {
    if (isBrowserRunning) {
      scr.type("w", keyMeta);
    }
    isBrowserRunning = false;
  }

  private static String currentTest = "";

  private static void before(String test, String text) {
    currentTest = test;
    p("***** starting %s %s", test, text);
  }

  private static void after() {
    p("***** ending %s", currentTest);
    showStop();
    browserStop();
  }

  private static List<Match> highlight(List<Match> regs, int time) {
    for (Match reg : regs) {
      reg.highlight();
    }
    scr.wait(time * 1.0);
    ScreenHighlighter.closeAll();
    return regs;
  }

  private static void highlight(List<Match> regs) {
    highlight(regs, 1);
  }

  private static List<Integer> runTest = new ArrayList<>();

  private static boolean shouldRunTest(int nTest) {
    if (runTest.contains(0) || runTest.contains(nTest)) {
      return true;
    }
    return false;
  }
  //</editor-fold>

  public static void main(String[] args) {
    String browser = "edge";
    if (runTime.runningMac) {
      browser = "safari";
      keyMeta = Key.CMD;
    }
    ImagePath.setBundlePath(new File(runTime.fWorkDir, showBase).getAbsolutePath());
    Match match = null;
    String testImage = "findBase";

//    runTest.add(0);
//    runTest.add(1); // exists
//    runTest.add(2); // findChange
//    runTest.add(3); // text OCR
//    runTest.add(4); // text find word
//    runTest.add(5); // text find lines RegEx
//    runTest.add(6); // text Region.find(someText)
//    runTest.add(7); // text Region.findAll(someText)
//    runTest.add(8); // text Region.getWordList/getLineList
//    runTest.add(9); // basic transparency
//    runTest.add(10); // transparency with pattern
//    runTest.add(11); // find SwitchToText

    if (runTest.size() > 1) {
      if (-1 < runTest.indexOf(0)) {
        runTest.remove(runTest.indexOf(0));
      }
    } else if (runTest.size() == 0) {
      before("test99", "play");
      //App.focus("safari"); scr.wait(1.0); reg = App.focusedWindow();
      after();
    }

    //<editor-fold desc="test1 exists">
    if (shouldRunTest(1)) {
      before("test1", "scr.exists(testImage)");
      show(testImage, 0);
      scr.wait(2.0);
      match = scr.exists(testImage, 10);
      match.highlight(2);
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test2 findChange">
    if (shouldRunTest(2)) {
      before("test2", "findChange");
      show(testImage, 0);
      scr.wait(2.0);
      Finder finder = new Finder(testImage);
      String imgChange = "findChange3";
      List<Region> changes = finder.findChanges(imgChange);
      match = scr.exists(testImage, 10);
      for (Region change : changes) {
        match.getInset(change).highlight(1);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test3 text OCR">
    if (shouldRunTest(3)) {
      before("test3", "text OCR");
      if (openTestPage()) {
        String text = "";
        if (Do.SX.isNotNull(reg)) {
          text = reg.text().trim();
        }
        p("***** read:\n%s", text);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test4 find word">
    if (shouldRunTest(4)) {
      before("test4", "findWord");
      String aWord = "brown";
      if (openTestPage()) {
        Match mText = reg.findWord(aWord);
        if (Do.SX.isNotNull(mText)) {
          mText.highlight(2);
          highlight(reg.findWords(aWord), 2);
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test5 findLines with RegEx">
    if (shouldRunTest(5)) {
      before("test5", "findLines with RegEx");
      String aRegex = "jumps.*?lazy";
      if (openTestPage()) {
        List<Match> matches = highlight(reg.findLines(Finder.asRegEx(aRegex)), 3);
        for (Match found : matches) {
          p("**** line: %s", found.getText());
        }
        aRegex = "jumps.*?very.*?lazy";
        matches = highlight(reg.findLines(Finder.asRegEx(aRegex)), 3);
        for (Match found : matches) {
          p("**** line: %s", found.getText());
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test6 Region.find(someText)">
    if (shouldRunTest(6)) {
      before("test6", "Region.find(someText)");
      String[] aTexts = new String[]{"another", "very, very lazy dog", "very + dog"};
      if (openTestPage()) {
        for (String aText : aTexts) {
          match = reg.existsText(aText);
          if (Do.SX.isNotNull(match)) {
            match.highlight(2);
          }
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test7 Region.findAll(someText)">
    if (shouldRunTest(7)) {
      before("test7", "Region.findAll(someText)");
      String aText = "very lazy dog";
      if (openTestPage()) {
        Match found = null;
        found = reg.hasText(Finder.asRegEx(aText));
        if (Do.SX.isNotNull(found)) {
          found.highlight(2);
        }
        highlight(reg.findAllText(aText), 2);
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test8 Region.getWordList/getLineList">
    if (shouldRunTest(8)) {
      before("test8", "Region.getWords/getLines");
      if (openTestPage()) {
        List<Match> lines = reg.collectLines();
        if (lines.size() > 0) {
          for (Match line : lines) {
            line.highlight(1);
            p("***** line: %s", line.getText());
          }
        }
        List<Match> words = reg.collectWords();
        if (words.size() > 0) {
          int jump = words.size() / 10;
          int current = 0;
          for (Match word : words) {
            if (current % 10 == 0) {
              word.highlight(1);
            }
            p("%s", word.getText());
            current++;
          }
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test9 basic transparency">
    if (shouldRunTest(9)) {
      before("test9", "basic transparency");
      Image img4 = Image.create("buttonText");
      Image img4O = Image.create("buttonTextOpa");
      Image img5 = Image.create("buttonTextTrans");
      if (openTestPage("Test-page-1")) {
        reg.highlight(1);
        Image image = img5;
        List<Match> matches = reg.findAllList(image);
        highlight(matches);
        for (Match next : matches) {
          p("Match: (%d,%d) %.6f", next.x, next.y, next.getScore());
          List<Match> wordList = next.collectLines();
//        Match match1 = next.grow(10).has(image);
//        p("Match1: (%d,%d) %.6f", match1.x, match1.y, match1.getScore());
          p("%s (text: %s)", wordList.get(0).getText(), next.text().trim());
        }
      }
      after();
    }
    //</editor-fold>

    //<editor-fold desc="test10 transparency with pattern">
    if (shouldRunTest(10)) {
      before("test10", "transparency with pattern");
      String wb = "whiteBlack";
      String wt = "whiteTrans";
      String wwt = "whiteWithText";
      //App.focus("preview"); scr.wait(1.0);
      show(wwt, 0);
      scr.wait(2.0);
      reg = scr;
      reg = App.focusedWindow();
      Pattern wbMask = new Pattern(wb).asMask();
      Pattern pWwtWb = new Pattern(wwt).withMask(wbMask);
      p("***** real image");
      reg.has(wwt);
      reg.highlight(-1);
      p("***** pattern asMask()");
      reg.has(wbMask);
      reg.highlight(-1);
      p("***** pattern withMask()");
      reg.has(pWwtWb);
      reg.highlight(-1);
      p("***** transparent masked image");
      reg.has(wt);
      reg.highlight(-1);
      after();
    }
    //</editor-fold>

    //<editor-fold desc="find SwitchToText">
    if (shouldRunTest(11)) {
      before("test11", "find SwitchToText");
      Settings.SwitchToText = true;
      String[] aTexts = new String[]{"another"};
      reg = scr;
      if (openTestPage()) {
        for (String aText : aTexts) {
          match = reg.has(aText);
          if (Do.SX.isNotNull(match)) {
            match.highlight(1);
          }
        }
      }
      after();
    }
    //</editor-fold>
  }
}