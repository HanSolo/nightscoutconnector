module eu.hansolo.nightscoutconnector {
    requires java.net.http;
    requires java.base;
    requires java.management;

    requires transitive eu.hansolo.toolbox;
    requires transitive com.google.gson;

    exports eu.hansolo.nightscoutconnector;
}