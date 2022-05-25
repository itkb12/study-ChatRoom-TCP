import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ArrayList<Conectador> connections;
    private ServerSocket server;
    private boolean terminado;
    private ExecutorService pool;

    public Server(){
        connections = new ArrayList<>();
        terminado = false;
    }
    
    @Override
    public void run(){
        
        try {
            server = new ServerSocket(1234);
            pool = Executors.newCachedThreadPool();
            while(!terminado){
                Socket client = server.accept();
                Conectador handler = new Conectador(client);
                connections.add(handler);
                pool.execute(handler);
            }
            
        } catch (Exception e) {
            desligar();
        }
    }

    public void broadcast(String mensagem){
        for(Conectador ch : connections){
            if(ch != null){
                ch.enviarMensagem(mensagem);
            }
        }
    }

    public void desligar(){
        try{
            terminado = true;
            pool.shutdown();
            if(!server.isClosed()){
                server.close();
            }
        }catch(IOException e){
            // ignora
        }
        for(Conectador c : connections){
            c.desligar();
        }
    }

    class Conectador implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String nickname;

        public Conectador(Socket client){
            this.client = client;
        }

        @Override
        public void run(){
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Teu nome: ");
                nickname = in.readLine();
                
                System.out.println(nickname + " entrou!");
                broadcast(nickname + " se juntou ao chat!");
                String mensagem;
                while ((mensagem = in.readLine()) != null){
                    if(mensagem.startsWith("/nick")){
                        String[] separarMensagem = mensagem.split(" ", 2);
                        if(separarMensagem.length == 2){
                            broadcast(nickname + " se renomeou para " + separarMensagem[1]);
                            System.out.println(nickname + " se renomeou para " + separarMensagem[1]);
                            nickname = separarMensagem[1];
                            out.println("Você se renomeou com sucesso!");
                        }else{
                            out.println("Nenhum nome foi dado.");
                        }
                    }else if(mensagem.startsWith("/sair")){
                        broadcast(nickname + "saiu.");
                        desligar();
                    }else{
                        broadcast(nickname + ": " + mensagem);
                    }
                }
            } catch (Exception e) {
                desligar();
            }
        }

        public void enviarMensagem(String mensagem){
            out.println(mensagem);
        }

        public void desligar(){
            try {
                in.close();
                out.close();
                if(!client.isClosed()){
                    client.close();
                }
            } catch (IOException e) {
                //ignora
            }

        }
    }
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
