import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

//預金額の時間変化を折れ線グラフ化するためのクラス(JPanelを拡張し、かつRunnableを実装)
public class AccountPanel extends JPanel implements Runnable{
	public static final int HORIZONTAL_SCALE = 500; //横軸（時間軸）の点の数N
	public static final int VERTICAL_SCALE = 12000; //縦軸のスケール（最大表示金額）
	public static final int REFRESH_INTERVAL = 15; //更新間隔（ミリ秒）
	private LinkedList <Integer> list; //直近の預金額をN個覚えるためのリスト
	private Account ac; //演習課題1のAccountクラス
	
	//コンストラクタ（引数にAccountクラス）
	AccountPanel(Account ac){
		super();
		this.ac = ac;
		/*リストの初期化処理（N個の初期値を格納したリストにする）
		 パネルの背景色などを設定*/
		list = new LinkedList<Integer>();
		for(int i = 0; i < HORIZONTAL_SCALE; i++){
			list.addLast(ac.getValue());
		}
		this.setBackground(Color.white);
	}
	
	//直近N個の預金額を格納したリストから折れ線グラフを表示する
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		int w = getWidth(); //パネルの横幅
		int h = getHeight(); //パネルの縦幅
		int x1;
		int y1;
		int x2;
		int y2;
		Label label = new Label();
		
		/*N個のデータの折れ線グラフを表示する処理*/
		for(int i = 0; i < HORIZONTAL_SCALE - 1; i++){
			x1 = (int)((double)w / HORIZONTAL_SCALE * i);
			y1 = (int)((double)h / VERTICAL_SCALE *list.get(i));
			x2 = (int)((double)w / HORIZONTAL_SCALE * (i+1));
			y2 = (int)((double)h / VERTICAL_SCALE *list.get(i+1));
			g.drawLine(x1, y1, x2, y2);
			
			if((list.get(i)-list.get(i+1)) != 0){
				String st = String.valueOf(list.get(i+1));
				if(list.get(i+1)==0){
					g.drawString(st,x2+5,y2+15);
				}
				else{
					g.drawString(st,x2,y2);
				}
			}
			
		}
	}
	
	//折れ線グラフの描画を一定間隔で繰り返すためのスレッド処理*/
	public void run(){
		/*以下の処理を繰り返し実行するようにする
		 「１．最新の預金額を取得してリスト末尾に追加し、最も古い値（リスト先頭）を削除」
		 「２．repaint()を呼び出して折れ線グラフの表示を更新」
		 「３．一定時間（REFRESH_INTERVALで指定した時間）sleep()する」
		 */
		
		while(true){
			list.addLast(ac.getValue());
			list.removeFirst();
			
			repaint();
			
			try{
				Thread.sleep(REFRESH_INTERVAL);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args){
		Account ac = new Account(); //演習課題１のAccountクラス
		AccountPanel ap = new AccountPanel(ac); //Accountオブジェクトをコンストラクタに渡す
		
		//フレームを作成し、AccountPanelを配置
		JFrame f = new JFrame();
		Container c = f.getContentPane();
		c.add(ap);
		f.setTitle("Account Frame");
		f.setSize(1000,300);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		
		/*スレッドの起動処理（３つ）
		 ・課題Ⅰで作成した預金側スレッド
		 ・課題Ⅰで作成した引き出し側スレッド
		 ・折れ線グラフ更新スレッド（AccountPanel自身がRunnableなのでapをThreadに渡す）
		 */
		
		DepositManager cam1 = new DepositManager("cam1", ac);
		WithdrawManager cam2 = new WithdrawManager("cam2", ac);
		Thread panelThread = new Thread(ap);

		cam1.start();
		cam2.start();
		panelThread.start();
		
	}
}

class Account {

	private int value = 10000;

	public void deposit(int money){
		synchronized(this){
			if(value >= 3000){
				notifyAll();
			}
			System.out.println(Thread.currentThread().getName() + "：入金がありました");
			value += money;

			System.out.println(Thread.currentThread().getName() + "：預金額：" + money + " " + "新規残額：" + value);
			System.out.println(Thread.currentThread().getName() + "：預金を完了しました");
		}
	}

	public void withdraw(int money){
		synchronized(this){
			while(value < 3000){
				try{
					System.out.println(Thread.currentThread().getName() + "：引き出し待機中");
					wait();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			notifyAll();

			System.out.println(Thread.currentThread().getName() + "：引き出し依頼がありました");
			value -= money;

			System.out.println(Thread.currentThread().getName() + "：出金額：" + money + " " + "新規残額：" + value);
			System.out.println(Thread.currentThread().getName() + "：引き出しを完了しました");
		}
	}
	
	int getValue(){
		return value;
	}

}
	
class DepositManager extends Thread{
	String name;
	Account targetAccount;
	
	public DepositManager(String name, Account ac){
		this.name = name;
		this.targetAccount = ac;
	}
	
	public void run(){
		while(true){
			try{
				Thread.sleep(10000);
				targetAccount.deposit(5000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
}
	
class WithdrawManager extends Thread{
	String name;
	Account targetAccount;
	
	public WithdrawManager(String name, Account ac){
		this.name = name;
		this.targetAccount = ac;
	}
	
	public void run(){
		while(true){
			try{
				Thread.sleep(3000);
				targetAccount.withdraw(3000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
}
