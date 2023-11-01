import logging.config
from functools import partial
from functools import singledispatch
from threading import Thread

from smpplib import command
from smpplib import consts
from smpplib import gsm
from smpplib.client import Client
from smpplib.pdu import PDU

from smsc_client import settings

__all__ = 'send'

logging.config.dictConfig(settings.LOGGING)
logger = logging.getLogger('SMSCApp2')

SMSC_HOST = settings.SMSC_HOST
SMSC_PORT = settings.SMSC_PORT


def extract_message_id(short_message: bytes) -> str:
    """Extract internal SMSC message id from `short_message` attribute.

    The reason is `deliver_sm` PDU has an empty `receipted_message_id`
    attribute.

    The following is an example of `short_message` attribute from `deliver_sm`
    PDU sent by SMSC:

    b'id:Smsc2108     \
      sub:1           \
      dlvrd:1         \
      submit          \
      date:2311011357 \
      done            \
      date:2311011357 \
      stat:DELIVRD    \
      err:0           \
      text:Hi everyone'
    """
    step1 = short_message.decode()
    step2 = step1.split()[0]
    step3 = step2.split(':')[1]
    return step3


@singledispatch
def handle_pdu(pdu: PDU):
    raise AttributeError(f'Unsupported type {type(pdu)}')


@handle_pdu.register
def handle_deliver_sm(pdu: command.DeliverSM):
    logger.info(
        f'[SMSC] message id={extract_message_id(pdu.short_message)} was delivered'
    )


@handle_pdu.register
def handle_submit_sm_resp(pdu: command.SubmitSMResp):
    logger.info(f'[SMSC] message was submitted, id={pdu.message_id.decode()}')


def send_message(client: Client, sender: str, recipient: str, message: str):
    parts, encoding_flag, msg_type_flag = gsm.make_parts(message)
    for part in parts:
        pdu = client.send_message(
            source_addr=sender,
            destination_addr=recipient,
            short_message=part,
            data_coding=encoding_flag,
            esm_class=msg_type_flag,
            source_addr_ton=consts.SMPP_TON_INTL,
            dest_addr_ton=consts.SMPP_TON_INTL,
            registered_delivery=True,
        )
        logger.debug(
            f'Message sent\n'
            f'> PDU {pdu.client=}\n'
            f'> PDU {pdu.command=}\n'
            f'> PDU {pdu.destination_addr=}\n'
            f'> PDU {pdu.length=}\n'
            f'> PDU {pdu.sequence=}\n'
            f'> PDU {pdu.short_message=}\n'
            f'> PDU {pdu.sm_length=}\n'
            f'> PDU {pdu.source_addr=}'
        )


client = Client(
    SMSC_HOST,
    SMSC_PORT,
    allow_unknown_opt_params=True,
    logger_name='smpp.Client',
    timeout=10,
)
client.set_message_received_handler(lambda pdu: handle_pdu(pdu))
client.set_message_sent_handler(lambda pdu: handle_pdu(pdu))

client.connect()
client.bind_transceiver(system_id='smppclient1', password='password')

send = partial(send_message, client)

# try:
#     client.listen()  # enters into a loop
# except KeyboardInterrupt:
#     logger.error('Interrupted, exiting...')
# finally:
#     client.unbind()
#     client.disconnect()

t = Thread(target=client.listen)
t.start()

if __name__ == '__main__':
    send(sender='123', recipient='321', message='Hi 1000')
    send(sender='123', recipient='321', message='Hi 2000')
    send(sender='123', recipient='321', message='Hi 3000')
