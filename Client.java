import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean terminado = false;

    public void run(){
        try {
            client = new Socket("127.0.0.1",1234);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            CuidadorDoInput inCuidador = new CuidadorDoInput();
            Thread t = new Thread(inCuidador);
            t.start();

            String inMessage;
            while((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
            
        } catch (Exception e) {
            desligar();
        }
    }

    public void desligar(){
        terminado = true;
        try {
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        } catch (Exception e) {
            //ignorar
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    class CuidadorDoInput implements Runnable{
        public void run(){
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));

                while(!terminado){
                    String mensagem = inReader.readLine();
                    if(mensagem.equals("/sair")){
                        out.println(mensagem);
                        inReader.close();
                        desligar();
                    }else{
                        out.println(mensagem);
                    }
                }
            } catch (Exception e) {
                desligar();
            }
        }

        
    }

    
}
