""" App settings.
"""

SMSC_HOST = '127.0.01'

SMSC_PORT = 2775

LOGGING = {
    'version': 1,
    'formatters': {
        'detailed': {
            'class': 'logging.Formatter',
            'format': '%(asctime)s %(name)s %(levelname)-6s %(message)s',
        },
        'colored': {
            '()': 'colorlog.ColoredFormatter',
            'format': '%(asctime)s %(log_color)s[%(levelname)-7s]%(reset)s'
            ' %(name)-12s %(message_log_color)s%(message)s',
            'datefmt': '%H:%M:%S',
            'secondary_log_colors': {
                'message': {
                    'DEBUG': 'light_cyan',
                    'INFO': 'green',
                    'WARNING': 'yellow',
                    'ERROR': 'red',
                    'CRITICAL': 'bold_red',
                },
            },
        },
    },
    'handlers': {
        'console': {
            'class': 'logging.StreamHandler',
            'level': 'DEBUG',
            'formatter': 'colored',
        },
        'file': {
            'class': 'logging.FileHandler',
            'filename': './smsc_client.log',
            'mode': 'a',
            'level': 'INFO',
            'formatter': 'detailed',
        },
    },
    'loggers': {
        'root': {
            'level': 'DEBUG',
            'handlers': ['console', 'file'],
            'propagate': False
        },
        'smpp.Client': {
            'level': 'DEBUG',
            'handlers': ['console'],
            'propagate': False
        },
    },
}
