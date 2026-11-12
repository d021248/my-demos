package d021248.mytry.demos.terminal;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/*Copyright (c) 2011 Aravind Rao
Modifications by Sam Barnum, 360Works 2012
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/// Swing-based file-picker launcher for {@link AsciiImageConverter}.
///
/// Opens a file chooser, converts the selected image to ASCII via
/// {@link AsciiImageConverter}, and displays the result in a scrollable dialog.
/// Pure conversion logic (without Swing dependency) lives in
/// {@link AsciiImageConverter}.
public final class AsciiArtFromImage {

  public static void main() {
    SwingUtilities.invokeLater(
        () -> {
          var fileChooser = new JFileChooser();
          fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "gif", "png"));
          while (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
              File f = fileChooser.getSelectedFile();
              BufferedImage image = ImageIO.read(f);
              if (image == null) throw new IllegalArgumentException(f + " is not a valid image.");
              String ascii = new AsciiImageConverter().convert(image);
              var textArea = new JTextArea(ascii, image.getHeight(), image.getWidth());
              textArea.setFont(new Font("Monospaced", Font.BOLD, 5));
              textArea.setEditable(false);
              var dialog =
                  new JOptionPane(new JScrollPane(textArea), JOptionPane.PLAIN_MESSAGE)
                      .createDialog(AsciiArtFromImage.class.getName());
              dialog.setResizable(true);
              dialog.setVisible(true);
            } catch (Exception e) {
              JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
          }
          System.exit(0);
        });
  }
}
