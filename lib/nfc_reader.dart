import 'dart:async';

import 'package:flutter/services.dart';

enum NFCStatus { none, reading, read, writing, write, stopped, error }

class NFCReadData {
  final String oid;
  final String data;
  final String error;
  final String currentStatus;

  NFCStatus nfcStatus;

  NFCReadData({this.oid, this.data, this.error, this.currentStatus});

  factory NFCReadData.fromMap(Map map) {
    NFCReadData result = NFCReadData(
        oid: map['nfcId'],
        data: map['nfcContent'],
        error: map['nfcError'],
        currentStatus: map['nfcStatus']);
    switch (result.currentStatus) {
      case 'none':
        result.nfcStatus = NFCStatus.none;
        break;
      case 'reading':
        result.nfcStatus = NFCStatus.reading;
        break;
      case 'read':
        result.nfcStatus = NFCStatus.read;
        break;
      case 'writing':
        result.nfcStatus = NFCStatus.writing;
        break;
      case 'stopped':
        result.nfcStatus = NFCStatus.stopped;
        break;
      case 'error':
        result.nfcStatus = NFCStatus.error;
        break;
      default:
        result.nfcStatus = NFCStatus.none;
    }
    return result;
  }

  @override
  String toString() {
    return this.data;
  }
}

class NfcReader {
  static final MethodChannel _channel = const MethodChannel('nfc_reader');

  static final _eventChannel =
      const EventChannel('com.trigsoft.nfc_read_write.nfc_reader');

  static Stream<dynamic> _tagStream;

  static Stream<NFCReadData> readNfc({
    bool isOnce = false,
  }) {
    if (_tagStream == null) {
      _tagStream = _eventChannel.receiveBroadcastStream().map((tag) {
        return NFCReadData.fromMap(tag);
      });
    }

    StreamController<NFCReadData> controller = StreamController();

    final stream = isOnce ? _tagStream.take(1) : _tagStream;

    stream.listen((message) {
      controller.add(message);
    }, onDone: () {
      _tagStream = null;
      return controller.close();
    });

    _channel.invokeMethod('readNFC');

    return controller.stream;
  }
}
