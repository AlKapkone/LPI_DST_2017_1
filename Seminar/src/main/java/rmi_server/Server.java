package rmi_server;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

public class Server implements Compute, Closeable {

    private Registry registry;
    private Compute stub;

    @Override
    public void close() throws IOException {
        closeRegistry();
        closeStub();
    }

    private void closeRegistry() throws IOException{
        if (registry != null) {
            try {
                registry.unbind(SERVER_NAME);
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
            registry = null;
        }
    }

    private void closeStub() throws IOException{
        if (stub != null) {
            UnicastRemoteObject.unexportObject(this, true);
            stub = null;
        }
    }

    private int port;

    public void startServer(int port){
        this.port = port;
        getSecurityManager();
        start();
    }

    private void getSecurityManager(){
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
    }

    private void start(){
        try {
            setRegistry();
        } catch (Exception ex) {
            System.err.println("ComputeEngine exception: ");
        }
    }

    private void setRegistry() throws Exception{
        registry = LocateRegistry.createRegistry(port);
        stub = (Compute) UnicastRemoteObject.exportObject(this, port);
        registry.rebind(Compute.SERVER_NAME, stub);
    }

    @Override
    public String ping() throws RemoteException {
        System.out.printf("%tF %<tT %s %n", new Date()," Sending ping response");
        return "pong";
    }

    @Override
    public String echo(String text) throws RemoteException {
        System.out.printf("%tF %<tT %s %n", new Date()," Sending echo response");
        return "ECHO: " + text;
    }

    @Override
    public <T> T executeTask(Task<T> t) throws IOException, RemoteException {
        System.out.printf("%tF %<tT %s %n", new Date()," Start sort");
        return t.execute();
    }
}
