package hu.bme.mogi.w81gpx.histogram;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.*;
import javax.swing.*;


public class hist1 extends JFrame {
    
	/** Elso Java programom, hurra! :)
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** hogy Mac-Windows-Linux alatt ne legyen keveredes a CR-LF-ekkel */
	static private final String newline = System.getProperty("line.separator");
		
	public hist1() {
		super("hist1");
		
		/** csinalunk egy szovegmezot, ide irjuk, hogy mi tortenik */
		final JTextArea log = new JTextArea(5,20);
		log.setMargin(new Insets(5,5,5,5));
		JScrollPane logScrollPane = new JScrollPane(log);
		
		/** megnyitas dialoghoz */
		final JFileChooser fc = new JFileChooser();
		
		/** egy tombbe rakjuk a 3x2^8 erteket majd */
		final int[] szinek;
		szinek = new int[768];
		
		/** ez kidob egy megnyitasos dialogust, ahol ki lehet valasztani a kepet */
		JButton openButton = new JButton("Kép betöltése…");
		openButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(hist1.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    log.append("Megnyitás: " + file.getName() + newline);
                    
            		BufferedImage kep = null;
            		try {
            			/** beolvassuk a kivalasztott fajlt */
            			kep = ImageIO.read(file);
            			/** mereteket kiolvassuk */
            			int width = kep.getWidth();
            			int height = kep.getHeight();
            			/** relativ gyakorisag szamitasahoz az osszes keppont szama */
            			Rajz.osszes = width * height;
            			log.append("Szélesseg: " + width + newline);
            			log.append("Magasság: " + height + newline);
            			
            			/** 3 tomb a 3 csatornanak */
            			int[] piros;
            			piros = new int[256];
            			int[] zold;
    					zold = new int[256];
            			int[] kek;
    					kek = new int[256];		
            			
    					/** megvannak a kep dimenzioi, for ciklusokkal megszamoljuk a pixeleket */
            			for (int i=0; i < width; i++) {
            				for (int j=0; j < height; j++) {
            					/** sorban vegigmegy az oszlopokon, s lekeri
            					 *  mindegyik pixel szinet
            					 */
            					int RGB = kep.getRGB(i, j);
            					//log.append("getRGB(" + i + ", " + j + "): " + RGB + newline);
            					/** igy adja vissza, biteket kell shiftelni */
            					int R = (RGB >> 16) & 0xff;
            					int G = (RGB >> 8) & 0xff;
            					int B = (RGB) & 0xff;
            					//log.append("R: " + R + "; G: " + G + "; B: " + B + newline);
            					
            					/** szamoljuk, hogy adott szin hanyszor szerepel a kepen */
            					piros[R]++;
            					zold[G]++;
            					kek[B]++;
            				}
            			}
            			/** egybedobjuk az egeszet, mert kesobb ugy jobb lesz */
            			System.arraycopy(piros, 0, szinek, 0, 256);
            			System.arraycopy(zold, 0, szinek, 256, 256);
            			System.arraycopy(kek, 0, szinek, 512, 256);
            			/** atadjuk a masik class-nak is */
            			Rajz.rszinek=szinek;

            			log.append("Tömbök feltöltve." + newline);
            			
            		} catch (IOException ioe) {
            			/** csak mert muszaj :) */
            			ioe.printStackTrace();
            		}
                    
                } else {
                    log.append("A folyamat megszakadt." + newline);
                }
            }
        });

        JButton drawButton = new JButton("Rajzolás…");
        /** ez csinal egy uj ablakot, s radobja a hisztogrammot :) */
        drawButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		Rajz.korr=false;
        		/*for (int i=0; i<768; i++) {
        			log.append(Integer.toString(szinek[i])+" ");
        		}*/
        		JFrame frame_r = new JFrame("Hisztogram");
		        //frame_r.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        frame_r.getContentPane().add(new Rajz());
		        /** olyan szeles es magas legyen az ablak, mint amilyennek kell lennie…
		         *  a +22 az insetek/fejlecek/ilyesmik miatt kell (Mac alatt ennyi, remelem Windows alatt is)
		         */
		        frame_r.setSize(256*Rajz.szelesseg, Rajz.magassag + 22);
		        frame_r.setVisible(true);
		        log.append("Hisztogram kirajzolva." + newline);
        	}
        });
        
        JButton draw2Button = new JButton("Bebukás és -égés nélkül…");
        /** mint fentebb, de 0 és 255 ertekuek nem lesznek rajta */
        draw2Button.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		Rajz.korr=true;
        		JFrame frame_r = new JFrame("Korrigált hisztogram");
		        frame_r.getContentPane().add(new Rajz());
		        frame_r.setSize(256*Rajz.szelesseg, Rajz.magassag + 22);
		        frame_r.setVisible(true);
		        log.append("Korrigált hisztogram kirajzolva." + newline);
        	}
        });
        
        /** ezt a harom gombot kidobjuk egy panelra */
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(drawButton);
        buttonPanel.add(draw2Button);
        
        /** a panelt meg a logot a contentPane-re */
        Container contentPane = getContentPane();
        contentPane.add(buttonPanel, BorderLayout.NORTH);
        contentPane.add(logScrollPane, BorderLayout.CENTER);
		
	}
		
	public static void main(String s[]) {
		JFrame frame = new hist1();
		frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
		/** form atmeretezese, hogy pont jo legyen */
        frame.pack();
        frame.setVisible(true);
	}
	

}


class Rajz extends JComponent {
	/** Ez rajzol :)
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** ebbe a masik class betolja az adatokat */
	public static int[] rszinek;
	public static int osszes;
	public static boolean korr;

	/** Ez a ket ertek atirhato! :)
	 *  a magassag a grafikon max. magassaga
	 *  a szelesseg azt adja meg, hogy egy "vonal" (=szinertek)
	 *  milyen szeles oszlopot jelentsen
	 */
	public static final int magassag = 128;
	public static final int szelesseg = 1;
	
	public void paint(Graphics g) {
		
		/** legyen azert egy feher hatter */
		g.setColor(new Color(1.0f,1.0f,1.0f));
		g.fillRect(0,0,256*szelesseg,magassag);

		/** az egeszet atporgetjuk egy uj tombbe, mert lehet, hogy
		 *  korrigalni kell, s akkor jo lenne, ha megmaradna
		 *  az eredeti tomb is, s nem kene ujraszamolni az egeszet
		 */
		int[] rszinek_l;
		rszinek_l = new int[768];
		for (int i=0; i<768; i++) {
			rszinek_l[i] = rszinek[i];
		}
		
		/** a bebukott es beegett pixelek figyelmen kivul hagyasa */
		if (korr) {
			rszinek_l[0]=rszinek_l[255]=rszinek_l[256]=rszinek_l[511]=rszinek_l[512]=rszinek_l[767]=0;
		}
		
		/** megkeressuk a tomb maximumat - ez pont toltse ki a kapott helyet
		 * ha esetleg kulon akarjuk, nem egymasra, akkor lehetnek kulon maximumok
		 */
		int maxertek = 0;
/*		int maxertek_r = 0;
		int maxertek_g = 0;
		int maxertek_b = 0; */
		for (int i=0; i<768; i++) {
			if (rszinek_l[i]>maxertek) {
				maxertek = rszinek_l[i];
			}
		}
/*		for (int i=0; i<256; i++) {
			if (rszinek_l[i]>maxertek_r) {
				maxertek_r = rszinek_l[i];
			}
		}
		for (int i=256; i<512; i++) {
			if (rszinek_l[i]>maxertek_g) {
				maxertek_g = rszinek_l[i];
			}
		}
		for (int i=512; i<768; i++) {
			if (rszinek_l[i]>maxertek_b) {
				maxertek_b = rszinek_l[i];
			}
		} */
		if (korr) {
			rszinek_l[0]=rszinek_l[255]=rszinek_l[256]=rszinek_l[511]=rszinek_l[512]=rszinek_l[767]=maxertek;
		}
		/** ha nulla ala menne, akkor 0.0 lenne mindig az oszto, ezert lebegopontozunk */
		float oszto = (float)maxertek/magassag;
/*		float oszto_r = (float)maxertek_r/magassag;
		float oszto_g = (float)maxertek_g/magassag;
		float oszto_b = (float)maxertek_b/magassag; */
		
		/** atkergetjuk inkabb egy uj tombbe az egeszet, ugy lesz a jo :)
		 *  igy meretezzuk at a kapott magassagnak megfelelove az egeszet
		 */
		int[] rszinek_korr;
		rszinek_korr = new int[768];
		for (int i=0; i<768; i++) {
			rszinek_korr[i] = Math.round(rszinek_l[i]/oszto);
		}
		
/*		int[] rszinek_korr_r;
		rszinek_korr_r = new int[768];
		for (int i=0; i<768; i++) {
			rszinek_korr_r[i] = Math.round(rszinek_l[i]/oszto_r);
		}
		int[] rszinek_korr_g;
		rszinek_korr_g = new int[768];
		for (int i=0; i<768; i++) {
			rszinek_korr_g[i] = Math.round(rszinek_l[i]/oszto_g);
		}
		int[] rszinek_korr_b;
		rszinek_korr_b = new int[768];
		for (int i=0; i<768; i++) {
			rszinek_korr_b[i] = Math.round(rszinek_l[i]/oszto_b);
		} */
		
		/** aztan kirajzoljuk a harom grafikont
		 *  Color RGBA-t kap, 1.0f a teljesen fedo, 0.0f a teljesen atlatszo 
		 */
		for (int i=0; i<256; i++) {
			g.setColor(new Color(1.0f,0.0f,0.0f,0.8f));
			//g.setColor(Color.red);
			g.fillRect(szelesseg*i, magassag-rszinek_korr[i], szelesseg, rszinek_korr[i]);
			//g.fillRect(szelesseg*i, magassag-rszinek_korr_r[i], szelesseg, rszinek_korr_r[i]);
		}
		for (int i=256; i<512; i++) {
			g.setColor(new Color(0.0f,1.0f,0.0f,0.8f));
			//g.setColor(Color.green);
			g.fillRect(szelesseg*(i-256), magassag-rszinek_korr[i], szelesseg, rszinek_korr[i]);
			//g.fillRect(szelesseg*i, magassag-rszinek_korr_g[i], szelesseg, rszinek_korr_g[i]);
		}
		for (int i=512; i<768; i++) {
			g.setColor(new Color(0.0f,0.0f,1.0f,0.8f));
			//g.setColor(Color.blue);
			g.fillRect(szelesseg*(i-512), magassag-rszinek_korr[i], szelesseg, rszinek_korr[i]);
			//g.fillRect(szelesseg*i, magassag-rszinek_korr_b[i], szelesseg, rszinek_korr_b[i]);
		}
	}
}

