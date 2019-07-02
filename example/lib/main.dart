import 'package:flutter/material.dart';
import 'dart:async';

import 'package:nfc_reader/nfc_reader.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  NFCReadData _nfcReadData;

  StreamSubscription<NFCReadData> _streamSubscription;

  @override
  void initState() {
    super.initState();
  }

  @override
  void dispose() {
    super.dispose();
    _streamSubscription?.cancel();
  }

  void startReading() {
    StreamSubscription<NFCReadData> subscription =
        NfcReader.readNfc().listen((tag) {
      setState(() {
        _nfcReadData = tag;
      });
      print(_nfcReadData);
    }, onDone: () {
      setState(() {
        _streamSubscription = null;
      });
    });

    setState(() {
      _streamSubscription = subscription;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('NFC READ EXAMPLE'),
        ),
        body: SafeArea(
          top: true,
          bottom: true,
          child: Center(
            child: ListView(
              children: <Widget>[
                Text(
                  _nfcReadData != null ? 'Id: ${_nfcReadData.oid}' : '',
                  textAlign: TextAlign.center,
                ),
                Text(
                  _nfcReadData != null ? 'Data: ${_nfcReadData.data}' : '',
                  textAlign: TextAlign.center,
                ),
                Text(
                  _nfcReadData != null
                      ? 'Status: ${_nfcReadData.currentStatus}'
                      : '',
                  textAlign: TextAlign.center,
                ),
                RaisedButton(
                  child: Text('Start NFC'),
                  onPressed: () {
                    startReading();
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
