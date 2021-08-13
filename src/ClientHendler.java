import com.sun.corba.se.spi.activation.Server;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHendler {
    Server server;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    private boolean authenticated;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {


                try {
                    while (true) {


                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            sendMsg("/end");
                            System.out.println("OffLine");
                            break;
                        }


                        if (str.startsWith("/auth")) {
                            String[] token = str.split("\\s+");
                            nickname = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (nikname != null) {
                                server.subscribe(this);
                                authenticated = true;
                                sendMsg("/authok " + nikname);
                                break;
                            } else {
                                sendMsg("incorrecktLogin");
                            }
                        }
                    }


                    while (authenticated) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            sendMsg("/end");
                            System.out.println("OffLine");
                            break;
                        }
                        server.broadcastMsg(this, str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
